package com.unifina.signalpath.remote;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.datasource.IStopListener;
import com.unifina.feed.ITimestamped;
import com.unifina.signalpath.*;
import com.unifina.utils.MapTraversal;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;
import java.net.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Functionality that is common to modules that make a HTTP request:
 *  - sync/async requests
 *  - body formatting
 *  - SSL
 *
 * Crucial benefit over simply doing Unirest.post: not blocking the whole canvas (Streamr thread) while request is pending
 */
public abstract class AbstractHttpModule extends ModuleWithSideEffects implements IEventRecipient, IStopListener {

	private static final Logger log = Logger.getLogger(AbstractHttpModule.class);

	protected static final String BODY_FORMAT_JSON = "application/json";
	protected static final String BODY_FORMAT_FORMDATA = "application/x-www-form-urlencoded";
	protected static final String BODY_FORMAT_PLAIN = "text/plain";
	protected static final String BODY_FORMAT_XML = "application/xml";

	public static int DEFAULT_TIMEOUT_SECONDS = 5;
	public static int MAX_CONNECTIONS = 10;

	protected String bodyContentType = BODY_FORMAT_JSON;
	protected boolean trustSelfSigned = false;
	protected boolean isAsync = false;
	protected int timeoutMillis = 1000 * DEFAULT_TIMEOUT_SECONDS;

	private transient Propagator asyncPropagator;
	private transient CloseableHttpAsyncClient cachedHttpClient;

	private boolean hasDebugLogged = false; // TODO: remove

	private static class DontVerifyStrategy implements TrustStrategy {
		public boolean isTrusted(X509Certificate[] var1, String var2) throws CertificateException {
			return true;
		}
	}

	/** This function is overridden so that the tests can inject a mock HttpAsyncClient */
	protected HttpAsyncClient getHttpClient() {
		if (cachedHttpClient == null) {
			if (trustSelfSigned) {
				try {
					SSLContext sslContext = SSLContexts
							.custom()
							.loadTrustMaterial(null, new DontVerifyStrategy())
							.build();
					cachedHttpClient = HttpAsyncClients.custom()
							.setMaxConnTotal(MAX_CONNECTIONS)
							.setMaxConnPerRoute(MAX_CONNECTIONS)
							.setSSLContext(sslContext)
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

	private void stopClient() {
		try {
			if (cachedHttpClient != null) {
				cachedHttpClient.close();
			}
		} catch (Exception e) {
			log.error("Closing HTTP client failed", e);
		}
	}

	@Override
	public void onStop() {
		stopClient();
	}

	@Override
	protected void finalize() throws Throwable {
		stopClient();
	}

	@Override
	public void initialize() {
		super.initialize();
		// copied from ModuleWithUI
		if (getGlobals().isRunContext()) {
			getGlobals().getDataSource().addStopListener(this);
		}
	}

	private SSLContext getSelfSignedSslContext() {
		SSLContext sslContext;
		try {
			sslContext = SSLContexts
					.custom()
					.loadTrustMaterial(null, new TrustSelfSignedStrategy())
					.build();
		} catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
			sslContext = null;
		}
		return sslContext;
	}

	@Override
	public Map<String,Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		ModuleOptions options = ModuleOptions.get(config);

		ModuleOption bodyContentTypeOption = new ModuleOption("bodyContentType", bodyContentType, ModuleOption.OPTION_STRING);
		bodyContentTypeOption.addPossibleValue(BODY_FORMAT_JSON, BODY_FORMAT_JSON);
		bodyContentTypeOption.addPossibleValue(BODY_FORMAT_FORMDATA, BODY_FORMAT_FORMDATA);
		options.add(bodyContentTypeOption);

		ModuleOption asyncOption = new ModuleOption("syncMode", isAsync ? "async" : "sync", ModuleOption.OPTION_STRING);
		asyncOption.addPossibleValue("asynchronous", "async");
		asyncOption.addPossibleValue("synchronized", "sync");
		options.add(asyncOption);

		options.add(new ModuleOption("trustSelfSigned", trustSelfSigned, ModuleOption.OPTION_BOOLEAN));
		options.add(new ModuleOption("timeoutSeconds", timeoutMillis/1000, ModuleOption.OPTION_INTEGER));

		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		bodyContentType = MapTraversal.getString(config, "options.bodyContentType.value", AbstractHttpModule.BODY_FORMAT_JSON);
		trustSelfSigned = MapTraversal.getBoolean(config, "options.trustSelfSigned.value");
		isAsync = MapTraversal.getString(config, "options.syncMode.value", "async").equals("async");

		Integer timeoutSeconds = MapTraversal.getInteger(config, "options.timeoutSeconds.value");
		if (timeoutSeconds != null) {
			timeoutMillis = 1000 * timeoutSeconds;
		}

		if (trustSelfSigned && getSelfSignedSslContext() == null) {
			trustSelfSigned = false;
			// TODO: notify user that self-signed certificates aren't supported
		}

		// HTTP module in async mode won't send outputs on SendOutput(),
		// 	but only in receive() where it creates its own Propagator
		setPropagationSink(isAsync);
	}

	@Override
	public void activateWithoutSideEffects() {
		String msg = "Real-time action by '" + getEffectiveName() + "' ignored in historical mode:\n\n" + getDummyNotificationMessage();
		getParentSignalPath().showNotification(msg);
	}

	/**
	 * In historical/side-effect-free mode a message is shown in the UI to notify that module was activated
	 * @return Message to show in UI
	 */
	protected String getDummyNotificationMessage() {
		return "HTTP request sent";
	}

	/**
	 * Prepare HTTP request based on module inputs
	 * @return HTTP request that will be sent to server
     */
	protected abstract HttpRequestBase createRequest();

	@Override
	public void activateWithSideEffects() {
		final HttpTransaction response = new HttpTransaction(getGlobals().time);

		// get HTTP request from subclass
		HttpRequestBase request = null;
		try {
			request = createRequest();
			String canvasId = null;
			if (getRootSignalPath() != null && getRootSignalPath().getCanvas() != null) {
				canvasId = getRootSignalPath().getCanvas().getId();
			}
			log.info("HTTP request " + request.toString() + " from canvas " + canvasId);
		} catch (Exception e) {
			response.errors.add("Constructing HTTP request failed");
			response.errors.add(e.getMessage());
			sendOutput(response);
			// propagate manually immediately, otherwise error won't ever be sent
			if (isAsync) {
				getPropagator().propagate();
			}
			return;
		}

		// SECURITY: check request is not sent to localhost (CORE-1008)
		// @see https://stackoverflow.com/questions/2406341/how-to-check-if-an-ip-address-is-the-local-host-on-a-multi-homed-system
		// TODO: larger blacklist; maybe all private ranges? https://stackoverflow.com/questions/22479214/detect-if-an-ip-is-local-or-public
		if (!localAddressesAreAllowed()) {
			try {
				InetAddress targetIP = InetAddress.getByName(request.getURI().getHost());
				if (targetIP.isAnyLocalAddress() || targetIP.isLoopbackAddress() || NetworkInterface.getByInetAddress(targetIP) != null) {
					throw new RuntimeException("Local HTTP calls not allowed");
				}
			} catch (UnknownHostException | SocketException | RuntimeException e) {
				response.errors.add("Bad target address: " + e.getMessage());
				sendOutput(response);
				// propagate manually immediately, otherwise error won't ever be sent
				if (isAsync) {
					getPropagator().propagate();
				}
				return;
			}
		}

		if (request instanceof HttpEntityEnclosingRequestBase && BODY_FORMAT_JSON.equals(bodyContentType)) {
			request.setHeader(HttpHeaders.ACCEPT, "application/json");
			request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		}	// FORMDATA headers are correct already if the entity is UrlEncodedFormEntity
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(timeoutMillis)
				.setConnectionRequestTimeout(timeoutMillis)
				.setSocketTimeout(timeoutMillis).build();
		request.setConfig(requestConfig);

		// if async: push server response into FeedEvent queue; it will later call this.receive
		final AbstractHttpModule self = this;
		final CountDownLatch latch = new CountDownLatch(1);
		final long startTime = System.currentTimeMillis();
		final boolean async = isAsync;
		HttpAsyncClient client = getHttpClient();
		Object httpResponseFuture = client.execute(request, new FutureCallback<HttpResponse>() {
			@Override
			public void completed(HttpResponse httpResponse) {
				response.response = httpResponse;
				returnResponse();
			}

			@Override
			public void failed(Exception e) {
				response.errors.add("Sending HTTP Request failed");
				response.errors.add(e.toString());
				returnResponse();
			}

			@Override
			public void cancelled() {
				response.errors.add("HTTP Request was cancelled");
				returnResponse();
			}

			private void returnResponse() {
				response.responseTime = System.currentTimeMillis() - startTime;
				response.timestamp = getGlobals().isRealtime() ? new Date() : getGlobals().time;
				if (async) {
					getGlobals().getDataSource().enqueueEvent(new FeedEvent<>(response, response.timestamp, self));
				} else {
					latch.countDown();	// goto latch.await() below
				}
			}
		});

		// TODO: remove
		if (!hasDebugLogged && getRootSignalPath() != null && getRootSignalPath().getCanvas() != null) {
			hasDebugLogged = true;
			log.info("Created HttpClient from canvas " + getRootSignalPath().getCanvas().getId());
			Set<Thread> threads = Thread.getAllStackTraces().keySet();
			for (Thread t : threads) {
				if (t.getName().startsWith("I/O dispatcher")) {
					log.info(t.getName());
				}
			}
			log.info("end of threads.");
		}

		if (!isAsync) {
			try {
				boolean done = latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
				if (!done) {
					response.errors.add("HTTP Request timed out after " + timeoutMillis + " ms");
				}
			} catch (InterruptedException e) {
				response.errors.add(e.getMessage());
			}
			sendOutput(response);
		}
	}

	@Override
	protected String getNotificationAboutActivatingWithoutSideEffects() {
		return getName() + ": Requests are not being made in historical mode by default. This can be changed in module options.";
	}

	protected boolean localAddressesAreAllowed() {
		return false;
	}

	/**
	 * Asynchronously handle server response, call comes from event queue
	 * @param event containing HttpTransaction created within sendOutput
	 */
	@Override
	public void receive(FeedEvent event) {
		if (event.content instanceof HttpTransaction) {
			sendOutput((HttpTransaction) event.content);
			getPropagator().propagate();
		} else {
			super.receive(event);
		}
	}

	private Propagator getPropagator() {
		if (asyncPropagator == null) {
			asyncPropagator = new Propagator(this);
		}
		return asyncPropagator;
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
		@Override
		protected List<PossibleValue> getPossibleValues() {
			return Arrays.asList(
					new PossibleValue("GET", "GET"),
					new PossibleValue("POST", "POST"),
					new PossibleValue("PUT", "PUT"),
					new PossibleValue("DELETE", "DELETE"),
					new PossibleValue("PATCH", "PATCH")
			);
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
