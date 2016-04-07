package com.unifina.signalpath.remote;

import com.unifina.signalpath.*;
import com.unifina.utils.MapTraversal;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONObject;
import org.codehaus.groovy.grails.web.json.JSONTokener;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Module that lets user make HTTP requests with maximum control over
 *  - how response is formed (e.g. sending both URL params AND body)
 *  - getting all response headers, statusCodes, etc.
 * Maps will be used as both Input and Output type, though JSON output can also be List
 * @see SimpleHttp for module that does input construction and output de-construction for you
 */
public class Http extends AbstractHttpModule {

	private VerbParameter verb = new VerbParameter(this, "verb");
	private StringParameter URL = new StringParameter(this, "URL", "");
	private MapParameter headers = new MapParameter(this, "headers");
	private MapParameter queryParams = new MapParameter(this, "params");

	private Input<Object> body = new Input<>(this, "body", "Object");
	private MapOutput responseHeaders = new MapOutput(this, "headers");
	private ListOutput errorOut = new ListOutput(this, "errors");
	private Output<Object> response = new Output<>(this, "data", "Object");
	private TimeSeriesOutput statusCode = new TimeSeriesOutput(this, "status code");
	private TimeSeriesOutput pingMillis = new TimeSeriesOutput(this, "ping(ms)");

	@Override
	public void init() {
		addInput(verb);
		verb.setUpdateOnChange(true);	// input name changes: POST -> body; GET -> trigger
		addInput(URL);
		addInput(queryParams);
		addInput(headers);
		addInput(body);
		addOutput(errorOut);
		addOutput(response);
		addOutput(statusCode);
		addOutput(pingMillis);
		addOutput(responseHeaders);
	}

	/** For bodyless verbs, "body" is only a "trigger" */
	@Override
	public void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		if (config.containsKey("inputs")) {
			// body.setDisplayName won't cut it; it will be re-read from config afterwards
			for (Map i : (List<Map>) config.get("inputs")) {
				if (i.get("name").equals("body")) {
					i.put("displayName", verb.hasBody() ? "body" : "trigger");
				}
			}

			// trigger should be driving and non-togglable
			if (!verb.hasBody()) { body.setDrivingInput(true); }
			body.canToggleDrivingInput = verb.hasBody();
		}
	}

	@Override
	public void sendOutput() {
		List<String> errors = new LinkedList<>();

		List<NameValuePair> queryPairs = new LinkedList<>();
		for (Object pair : queryParams.getValue().entrySet()) {
			Map.Entry p = (Map.Entry)pair;
			NameValuePair nvp = new BasicNameValuePair(p.getKey().toString(), p.getValue().toString());
			queryPairs.add(nvp);
		}
		boolean alreadyAdded = (URL.getValue().indexOf('?') > -1);
		String url = URL.getValue() + (alreadyAdded ? "&" : "?") + URLEncodedUtils.format(queryPairs, "UTF-8");

		HttpRequestBase request = verb.getRequest(url);
		for (Object pair : headers.getValue().entrySet()) {
			Map.Entry p = (Map.Entry)pair;
			request.addHeader(p.getKey().toString(), p.getValue().toString());
		}

		if (verb.hasBody()) {
			try {
				switch (bodyFormat) {
					case BODY_FORMAT_JSON:
						Object b = body.getValue();
						String bodyString = b instanceof Map ? new JSONObject((Map)b).toString() :
											b instanceof List ? new JSONArray((List)b).toString() :
											b.toString();
						((HttpEntityEnclosingRequestBase)request).setEntity(new StringEntity(bodyString));
						break;
					case BODY_FORMAT_FORMDATA:
						Map bodyMap = (Map)body.getValue();
						List<NameValuePair> inputNVPList = new LinkedList<>();
						for (Object entry : bodyMap.entrySet()) {
							Map.Entry e = (Map.Entry)entry;
							inputNVPList.add(new BasicNameValuePair(e.getKey().toString(), e.getValue().toString()));
						}
						((HttpEntityEnclosingRequestBase)request).setEntity(new UrlEncodedFormEntity(inputNVPList));
						break;
					default:
						throw new RuntimeException("Unexpected body format " + bodyFormat);
				}
			} catch (UnsupportedEncodingException e) {
				errors.add(e.getMessage());
			}
		}

		HttpResponse httpResponse;
		try {
			long startTime = System.currentTimeMillis();
			httpResponse = getHttpClient().execute(request);
			pingMillis.send(System.currentTimeMillis() - startTime);

			String responseString = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			JSONTokener parser = new JSONTokener(responseString);
			Object responseData = parser.nextValue();	// parser returns Map, List, or String
			response.send(responseData);

			Map<String, String> headerMap = new HashMap<>();
			for (Header h : httpResponse.getAllHeaders()) {
				headerMap.put(h.getName(), h.getValue());
			}
			responseHeaders.send(headerMap);
			statusCode.send(httpResponse.getStatusLine().getStatusCode());
		} catch (IOException e) {
			errors.add(e.getMessage());
		}

		if (!errors.isEmpty()) {
			errorOut.send(errors);
		}
	}
}
