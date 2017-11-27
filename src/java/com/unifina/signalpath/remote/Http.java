package com.unifina.signalpath.remote;
import com.google.gson.Gson;
import com.unifina.signalpath.*;

import com.unifina.signalpath.text.JsonParser;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONException;
import org.codehaus.groovy.grails.web.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
	private ListOutput errors = new ListOutput(this, "errors");
	private Output<Object> responseData = new Output<>(this, "data", "Object");
	private TimeSeriesOutput statusCode = new TimeSeriesOutput(this, "statusCode");
	private TimeSeriesOutput roundtripMillis = new TimeSeriesOutput(this, "roundtripMillis");

	@Override
	public void init() {
		addInput(verb);
		verb.setUpdateOnChange(true);	// input name changes: POST -> body; GET -> trigger
		addInput(URL);
		addInput(queryParams);
		addInput(headers);
		addInput(body);
		addOutput(errors);
		addOutput(responseData);
		addOutput(statusCode);
		addOutput(roundtripMillis);
		addOutput(responseHeaders);
	}

	/** For bodyless verbs, "body" is only a "trigger" */
	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		if (config.containsKey("inputs")) {
			// body.setDisplayName won't cut it; it will be re-read from config afterwards
			for (Map i : (List<Map>)config.get("inputs")) {
				if (i.get("name").equals("body")) {
					i.put("displayName", verb.hasBody() ? "body" : "trigger");
				}
			}

			// trigger should be driving and non-togglable
			if (!verb.hasBody()) { body.setDrivingInput(true); }
			body.setCanToggleDrivingInput(verb.hasBody());
		}
	}

	/**
	 * Prepare HTTP request based on module inputs
	 * @return HTTP request that will be sent to server
	 */
	@Override
	protected HttpRequestBase createRequest() {
		String url = URL.getValue();
		if (queryParams.getValue().size() > 0) {
			List<NameValuePair> queryPairs = new LinkedList<>();
			for (Object pair : queryParams.getValue().entrySet()) {
				Map.Entry p = (Map.Entry) pair;
				NameValuePair nvp = new BasicNameValuePair(p.getKey().toString(), p.getValue().toString());
				queryPairs.add(nvp);
			}
			boolean alreadyAdded = (URL.getValue().indexOf('?') > -1);
			url += (alreadyAdded ? "&" : "?") + URLEncodedUtils.format(queryPairs, "UTF-8");
		}

		HttpRequestBase request = verb.getRequest(url);
		for (Object pair : headers.getValue().entrySet()) {
			Map.Entry p = (Map.Entry) pair;
			request.addHeader(p.getKey().toString(), p.getValue().toString());
		}

		if (verb.hasBody()) {
			try {
				switch (bodyContentType) {
					case BODY_FORMAT_JSON:
						Object b = body.getValue();
						String bodyString = new Gson().toJson(b);
						((HttpEntityEnclosingRequestBase) request).setEntity(new StringEntity(bodyString));
						break;
					case BODY_FORMAT_FORMDATA:
						Map bodyMap = (Map) body.getValue();
						List<NameValuePair> inputNVPList = new LinkedList<>();
						for (Object entry : bodyMap.entrySet()) {
							Map.Entry e = (Map.Entry) entry;
							inputNVPList.add(new BasicNameValuePair(e.getKey().toString(), e.getValue().toString()));
						}
						((HttpEntityEnclosingRequestBase) request).setEntity(new UrlEncodedFormEntity(inputNVPList));
						break;
					default:
						throw new RuntimeException("Unexpected body format " + bodyContentType);
				}
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}

		return request;
	}

	/**
	 * Send module output based on server response
	 * @param call and response from HTTP server plus metadata
	 */
	@Override
	protected void sendOutput(HttpTransaction call) {
		if (call.response != null) {
			try {
				// entity is null for bodyless response 204, and that's not an error
				HttpEntity entity = call.response.getEntity();
				if (entity != null) {
					if (isOctetStream(entity)) {
						responseData.send(EntityUtils.toByteArray(entity));
					} else {
						String responseString = EntityUtils.toString(entity, "UTF-8");
						if (responseString.isEmpty()) {
							call.errors.add("Empty response from server");
						} else {
							responseData.send(JsonParser.jsonStringToOutputObject(responseString));
						}
					}
				}

				Map<String, String> headerMap = new HashMap<>();
				for (Header h : call.response.getAllHeaders()) {
					headerMap.put(h.getName(), h.getValue());
				}
				responseHeaders.send(headerMap);
				statusCode.send(call.response.getStatusLine().getStatusCode());
			} catch (IOException | JSONException e) {
				call.errors.add(e.getMessage());
			}
		}

		roundtripMillis.send(call.responseTime);

		if (call.errors.size() > 0) {
			errors.send(call.errors);
		}
	}

	private static boolean isOctetStream(HttpEntity entity) {
		ContentType contentType = ContentType.get(entity);
		return contentType != null && contentType.getMimeType().equals(ContentType.APPLICATION_OCTET_STREAM.getMimeType());
	}
}
