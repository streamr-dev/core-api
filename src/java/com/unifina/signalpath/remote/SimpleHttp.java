package com.unifina.signalpath.remote;
import com.unifina.signalpath.*;

import com.unifina.utils.MapTraversal;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONException;
import org.codehaus.groovy.grails.web.json.JSONObject;
import org.codehaus.groovy.grails.web.json.JSONTokener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Module that makes HTTP requests and sends forward responses.
 * It constructs request input from variable number of Inputs, and
 *    de-constructs response output into specified Outputs using dot-notation names (e.g. values[3].car.id)
 * This module makes assumptions from input (GET uses URL params, POST uses body) and response (first found object is what we want)
 *    and makes a best-effort guess in case of "bad" response (if no object is found, just send values that were found)
 * @see MapTraversal that does output parsing according to Output name
 * @see Http for a minimally-magical module that gives full control to the user over forming the request and parsing the response
 */
public class SimpleHttp extends AbstractHttpModule {

	private VerbParameter verb = new VerbParameter(this, "verb");
	private StringParameter URL = new StringParameter(this, "URL", "");

	// subset of inputs/outputs that correponds to HTTP parameters and response data
	private List<Input<Object>> httpInputs = new ArrayList<>();
	private List<Output<Object>> httpOutputs = new ArrayList<>();

	// subset of inputs that corresponds to HTTP headers
	private List<StringParameter> headers = new ArrayList<>();

	private ListOutput errors = new ListOutput(this, "errors");
	private Input<Object> trigger = new Input<>(this, "trigger", "Object");

	@Override
	public void init() {
		addInput(verb);
		addInput(URL);
		trigger.setCanToggleDrivingInput(false);
		trigger.setDrivingInput(true);
		addOutput(errors);
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		ModuleOptions options = ModuleOptions.get(config);
		options.add(new ModuleOption("inputCount", httpInputs.size(), ModuleOption.OPTION_INTEGER));
		options.add(new ModuleOption("outputCount", httpOutputs.size(), ModuleOption.OPTION_INTEGER));
		options.add(new ModuleOption("headerCount", headers.size(), ModuleOption.OPTION_INTEGER));
		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		int inputCount = MapTraversal.getInt(config, "options.inputCount.value", 0);
		int outputCount = MapTraversal.getInt(config, "options.outputCount.value", 0);
		int headerCount = MapTraversal.getInt(config, "options.headerCount.value", 0);

		httpInputs = new ArrayList<>(inputCount);
		for (int i = 0; i < inputCount; i++) {
			Input<Object> in = new Input<>(this, "in"+(i+1), "Object");
			addInput(in);
			httpInputs.add(in);
		}
		if (inputCount == 0) {
			addInput(trigger);
		}
		httpOutputs = new ArrayList<>(outputCount);
		for (int i = 0; i < outputCount; i++) {
			Output<Object> out = new Output<>(this, "out"+(i+1), "Object");
			addOutput(out);
			httpOutputs.add(out);
		}
		headers = new ArrayList<>(headerCount);
		for (int i = 0; i < headerCount; i++) {
			StringParameter header = new StringParameter(this, "header"+(i+1), "");
			header.setCanToggleDrivingInput(false);
			header.setDrivingInput(false);
			addInput(header);
			headers.add(header);
		}
	}

	/**
	 * Prepare HTTP request based on module inputs
	 * @return HTTP request that will be sent to server
	 */
	@Override
	protected HttpRequestBase createRequest() {
		// read module inputs
		List<NameValuePair> inputNVPList = new LinkedList<>();
		JSONObject inputObject = new JSONObject();
		for (Input in : httpInputs) {
			String inputName = in.getEffectiveName();
			inputNVPList.add(new BasicNameValuePair(inputName, in.getValue().toString()));
			inputObject.put(inputName, in.getValue());
		}

		// build and prepare the HttpRequest: inputs are added to URL parameter string if body is not available
		HttpRequestBase request = verb.getRequest(URL.getValue());
		if (verb.hasBody()) {
			try {
				switch (bodyContentType) {
					case BODY_FORMAT_JSON:
						String bodyString = inputObject.toString();
						((HttpEntityEnclosingRequestBase) request).setEntity(new StringEntity(bodyString));
						break;
					case BODY_FORMAT_FORMDATA:
						((HttpEntityEnclosingRequestBase) request).setEntity(new UrlEncodedFormEntity(inputNVPList));
						break;
					default:
						throw new RuntimeException("Unexpected body format " + bodyContentType);
				}
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		} else if (inputNVPList.size() > 0) {
			boolean alreadyAdded = (URL.getValue().indexOf('?') > -1);
			String url = URL.getValue() + (alreadyAdded ? "&" : "?") + URLEncodedUtils.format(inputNVPList, "UTF-8");
			request = verb.getRequest(url);
		}

		for (StringParameter header : headers) {
			String headerName = header.getEffectiveName();
			request.addHeader(headerName, header.getValue());
		}

		return request;
	}

	/**
	 * Send module output based on server response
	 * @param call and response from HTTP server plus metadata
	 */
	@Override
	protected void sendOutput(HttpTransaction call) {
		if (call.response == null) {
			errors.send(call.errors);
			return;
		}

		// get from server either JSON object with values for named outputs, or list of output values
		List<Object> values = new LinkedList<>();
		JSONObject result = null;
		try {
			// entity is null for bodyless response 204, and that's not an error
			HttpEntity entity = call.response.getEntity();
			if (entity != null) {
				// parse response into a JSONObject (or simple list of values)
				String responseString = EntityUtils.toString(entity, "UTF-8");
				if (responseString.isEmpty()) {
					call.errors.add("Empty response from server");
				} else {
					JSONTokener parser = new JSONTokener(responseString);
					Object response = parser.nextValue();
					if (response instanceof JSONObject) {
						// TODO: http://jsonapi.org/format/ suggests we should read "data" member if available
						result = (JSONObject) response;
					} else if (response instanceof JSONArray) {
						// array => send first-found object to outputs, or send one JSON value for each output if found first
						result = findJSONObjectFromJSONArray((JSONArray) response, values, httpOutputs.size());
					} else {
						// first raw JSON value => send values to outputs as-is
						values.add(response);
						while (parser.more() && values.size() < httpOutputs.size()) {
							values.add(parser.nextValue());
						}
					}
				}
			}
		} catch (IOException | JSONException e) {
			call.errors.add(e.getMessage());
			// send out what values were read so far
		}

		// send results to outputs, match output names to result object keys (ignore extra result object members)
		if (result != null) {
			for (Output out : httpOutputs) {
				String key = out.getEffectiveName();
				Object value = MapTraversal.getProperty(result, key);
				if (value != null) {
					out.send(value);
				} else {
					call.errors.add(key + " not found (or was null) in HTTP response!");
				}
			}
		} else {
			// ...or just send the found values in order, one for each output
			Iterator<Object> iV = values.iterator();
			Iterator<Output<Object>> iO = httpOutputs.iterator();
			while (iV.hasNext() && iO.hasNext()) {
				iO.next().send(iV.next());
			}
		}

		if (call.errors.size() > 0) {
			errors.send(call.errors);
		}
	}

	/**
	 * Walk arrays, grab first found object; or collect given number of raw JSON values
	 * @param jsonValues that were found IF return value is null. If return value is not null, jsonValues is invalid.
	 * @return JSONObject that was first found, or null if $maxCount jsonValues were found first
	 */
	private JSONObject findJSONObjectFromJSONArray(JSONArray arr, List<Object> jsonValues, int maxCount) {
		for (int i = 0; i < arr.length() && jsonValues.size() < maxCount; i++) {
			Object ob = arr.opt(i);
			if (ob instanceof JSONArray) {
				ob = findJSONObjectFromJSONArray((JSONArray)ob, jsonValues, maxCount);
			} else {
				jsonValues.add(ob);
			}

			if (ob instanceof JSONObject) {
				return (JSONObject)ob;
			}
		}
		return null;
	}
}
