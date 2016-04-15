package com.unifina.signalpath.remote;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.feed.ITimestamped;
import com.unifina.signalpath.*;
import com.unifina.utils.MapTraversal;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Functionality that is common to HTTP modules:
 *  - sync/async requests
 *  - body formatting
 *  - SSL
 */
public abstract class AbstractHttpModule extends AbstractSignalPathModule implements IEventRecipient {

	protected static final String BODY_FORMAT_JSON = "application/json";
	protected static final String BODY_FORMAT_FORMDATA = "application/x-www-form-urlencoded";
	protected static final String BODY_FORMAT_PLAIN = "text/plain";
	protected static final String BODY_FORMAT_XML = "application/xml";

	public static int DEFAULT_TIMEOUT_SECONDS = 30;
	public static int MAX_CONNECTIONS = 10;

	protected String bodyContentType = BODY_FORMAT_JSON;
	protected boolean trustSelfSigned = false;
	protected boolean isAsync = false;
	protected int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;

	private transient CloseableHttpAsyncClient cachedHttpClient;

	/** This function is overridden so that the tests can inject a mock HttpAsyncClient */
	protected HttpAsyncClient getHttpClient() {
		if (cachedHttpClient == null) {
			if (trustSelfSigned) {
				try {
					SSLContext sslcontext = SSLContexts
							.custom()
							.loadTrustMaterial(null, new TrustSelfSignedStrategy())
							.build();
					cachedHttpClient = HttpAsyncClients.custom()
							.setMaxConnTotal(MAX_CONNECTIONS)
							.setMaxConnPerRoute(MAX_CONNECTIONS)
							.setSSLContext(sslcontext)
							.build();
				} catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
					trustSelfSigned = false;
					// TODO: notify user that self-signed certificates aren't supported
				}
			}
			if (!trustSelfSigned) {
				cachedHttpClient = HttpAsyncClients.custom()
						.setMaxConnTotal(MAX_CONNECTIONS)
						.setMaxConnPerRoute(MAX_CONNECTIONS)
						.build();
			}
			cachedHttpClient.start();
		}
		return cachedHttpClient;
	}

	@Override
	public Map<String,Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		ModuleOptions options = ModuleOptions.get(config);

		ModuleOption bodyContentTypeOption = new ModuleOption("bodyContentType", bodyContentType, ModuleOption.OPTION_STRING);
		bodyContentTypeOption.addPossibleValue(AbstractHttpModule.BODY_FORMAT_JSON, AbstractHttpModule.BODY_FORMAT_JSON);
		bodyContentTypeOption.addPossibleValue(AbstractHttpModule.BODY_FORMAT_FORMDATA, AbstractHttpModule.BODY_FORMAT_FORMDATA);
		options.add(bodyContentTypeOption);

		ModuleOption asyncOption = new ModuleOption("syncMode", isAsync ? "async" : "sync", ModuleOption.OPTION_BOOLEAN);
		asyncOption.addPossibleValue("asynchronous", "async");
		asyncOption.addPossibleValue("synchronized", "sync");
		options.add(asyncOption);

		options.add(new ModuleOption("trustSelfSigned", trustSelfSigned, ModuleOption.OPTION_BOOLEAN));
		options.add(new ModuleOption("timeoutSeconds", timeoutSeconds, ModuleOption.OPTION_INTEGER));

		return config;
	}

	@Override
	public void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		bodyContentType = MapTraversal.getString(config, "options.bodyContentType.value", AbstractHttpModule.BODY_FORMAT_JSON);
		trustSelfSigned = MapTraversal.getBoolean(config, "options.trustSelfSigned.value");
		isAsync = MapTraversal.getString(config, "options.syncMode.value", "async").equals("async");
		timeoutSeconds = MapTraversal.getInt(config, "options.timeoutSeconds.value", DEFAULT_TIMEOUT_SECONDS);

		// HTTP module in async mode won't send outputs on SendOutput(),
		// 	but only in receive() where it creates its own Propagator
		propagationSink = isAsync;

		// try getting HTTP client, resets trustSelfSigned if such SSL client can't be created
		getHttpClient();
	}

	/**
	 * Prepare HTTP request based on module inputs
	 * @return HTTP request that will be sent to server
     */
	protected abstract HttpRequestBase createRequest();

	@Override
	public void sendOutput() {
		final HttpTransaction response = new HttpTransaction(globals.isRealtime() ? new Date() : globals.time);

		HttpRequestBase request = null;
		try {
			request = createRequest();
		} catch (Exception e) {
			response.errors.add("Constructing HTTP request failed");
			response.errors.add(e.getMessage());
			sendOutput(response);
			return;
		}

		// if async: push server response into FeedEvent queue; it will later call this.receive
		final AbstractHttpModule self = this;
		final CountDownLatch latch = new CountDownLatch(1);
		final long startTime = System.currentTimeMillis();
		final boolean async = isAsync;
		HttpAsyncClient client = getHttpClient();
		Object ret = client.execute(request, new FutureCallback<HttpResponse>() {
			@Override
			public void completed(HttpResponse httpResponse) {
				response.response = httpResponse;
				returnResponse();
			}

			@Override
			public void failed(Exception e) {
				response.errors.add("Sending HTTP Request failed");
				response.errors.add(e.getMessage());
				returnResponse();
			}

			@Override
			public void cancelled() {
				response.errors.add("HTTP Request was cancelled");
				returnResponse();
			}

			private void returnResponse() {
				response.responseTime = System.currentTimeMillis() - startTime;
				response.timestamp = globals.isRealtime() ? new Date() : globals.time;
				if (async) {
					globals.getDataSource().getEventQueue().enqueue(new FeedEvent<>(response, response.timestamp, self));
				} else {
					latch.countDown();	// goto latch.await() below
				}
			}
		});

		if (!isAsync) {
			try {
				boolean done = latch.await(timeoutSeconds, TimeUnit.SECONDS);
				if (!done) {
					response.errors.add("HTTP Request timed out after " + timeoutSeconds + " seconds");
				}
			} catch (InterruptedException e) {
				response.errors.add(e.getMessage());
			}
			sendOutput(response);
		}
	}

	/**
	 * Asynchronously handle server response, call comes from event queue
	 * @param event containing HttpTransaction created within sendOutput
	 */
	@Override
	public void receive(FeedEvent event) {
		if (event.content instanceof HttpTransaction) {
			sendOutput((HttpTransaction) event.content);
			setSendPending(true);
			Propagator p = new Propagator(this);
			p.propagate();
		} else {
			super.receive(event);
		}
	}

	/**
	 * Send module output based on server response
	 * @param call and response from HTTP server plus metadata
	 */
	protected abstract void sendOutput(HttpTransaction call);

	@Override
	public void clearState() {}

	public static class VerbParameter extends StringParameter {
		public VerbParameter(AbstractSignalPathModule owner, String name) {
			super(owner, name, "POST"); //this.getValueList()[0]);
		}
		private List<PossibleValue> getValueList() {
			return Arrays.asList(
				new PossibleValue("GET", "GET"),
				new PossibleValue("POST", "POST"),
				new PossibleValue("PUT", "PUT"),
				new PossibleValue("DELETE", "DELETE"),
				new PossibleValue("PATCH", "PATCH")
			);
		}
		@Override public Map<String, Object> getConfiguration() {
			Map<String, Object> config = super.getConfiguration();
			config.put("possibleValues", getValueList());
			return config;
		}
		public boolean hasBody() {
			String v = this.getValue();
			return v.equals("POST") || v.equals("PUT") || v.equals("PATCH");
		}
		public HttpRequestBase getRequest(String url) {
			String v = this.getValue();
			return v.equals("GET") ? new HttpGet(url) :
				   	v.equals("POST") ? new HttpPost(url) :
					v.equals("PUT") ? new HttpPut(url) :
					v.equals("DELETE") ? new HttpDelete(url) :
					v.equals("PATCH") ? new HttpPatch(url) : new HttpGet(url);
		}
	}

	/** Keep track of the HTTP request and response */
	protected static class HttpTransaction implements ITimestamped {
		public HttpResponse response = null;
		public long responseTime = 0;
		public List<String> errors = new LinkedList<>();
		public Date timestamp;

		public HttpTransaction(Date timestamp) {
			this.timestamp = timestamp;
		}

		@Override
		public Date getTimestamp() {
			return timestamp;
		}
	}
}
