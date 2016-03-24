package com.unifina.signalpath.remote;
import com.unifina.signalpath.*;

import java.util.*;
import com.google.common.collect.ImmutableMap;
import com.unifina.utils.MapTraversal;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONObject;
import org.codehaus.groovy.grails.web.json.JSONTokener;

import java.io.IOException;

/**
 * Module that makes HTTP requests and sends forward responses.
 * It constructs request input from variable number of Inputs, and
 *    de-constructs response output into specified Outputs using dot-notation names (e.g. values[3].car.id)
 * This module makes assumptions with input (GET uses URL params, POST uses body) and output (first found object is what we want)
 *    and is quite lenient with "bad" output (if no object is found, just send values that were found)
 * @see MapTraversal that does output parsing according to Output displayName
 * @see Http for a minimally-magical module that gives full control to the user over forming the request and parsing the response
 */
public class SimpleHttp extends AbstractSignalPathModule {

	private HttpVerbParameter verb = new HttpVerbParameter(this, "verb");
	private StringParameter URL = new StringParameter(this, "URL", "http://localhost");

	// subset of inputs/outputs that correponds to HTTP parameters and response data
	private List<Input<Object>> httpInputs = new ArrayList<>();
	private List<Output<Object>> httpOutputs = new ArrayList<>();

	// subset of inputs that corresponds to HTTP headers
	private List<StringParameter> headers = new ArrayList<>();

	private StringOutput errorOut = new StringOutput(this, "error");
	private Input<Object> trigger = new Input<>(this, "trigger", "Object");

	private static final Logger log = Logger.getLogger(SimpleHttp.class);

	@Override
	public void init() {
		addInput(verb);
		addInput(URL);
		addInput(trigger);
		trigger.canToggleDrivingInput = false;
		trigger.setDrivingInput(true);
		addOutput(errorOut);
	}

	/** This function is overridden in SimpleHttpSpec to inject mock HttpClient */
	protected HttpClient getHttpClient() {
		if (_httpClient == null) {
			// commented out: SSL client that supports self-signed certs
			/*SSLContext sslcontext = SSLContexts.custom()
				.loadTrustMaterial(null, new TrustSelfSignedStrategy())
				.build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext);*/
			_httpClient = HttpClients.createMinimal(); /* .custom()
				.setSSLSocketFactory(sslsf)
				.build();*/
		}
		return _httpClient;
	}
	private transient CloseableHttpClient _httpClient;

	@Override
	public Map<String,Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		config.put(
			"options", ImmutableMap.of(
				"inputCount", ImmutableMap.of(
					"value", httpInputs.size(),
					"type", "int"
				),
				"outputCount", ImmutableMap.of(
					"value", httpOutputs.size(),
					"type", "int"
				),
				"headerCount", ImmutableMap.of(
					"value", headers.size(),
					"type", "int"
				)
			)
		);
		return config;
	}

	@Override
	public void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		int inputCount = MapTraversal.getInt(config, "options.inputCount.value", 0);
		int outputCount = MapTraversal.getInt(config, "options.outputCount.value", 0);
		int headerCount = MapTraversal.getInt(config, "options.headerCount.value", 0);

		httpInputs = new ArrayList<>(inputCount);
		for (int i = 0; i < inputCount; i++) {
			Input<Object> in = new Input<>(this, "in"+(i+1), "Object");
			in.canToggleDrivingInput = false;
			in.setDrivingInput(false);
			addInput(in);
			httpInputs.add(in);
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
			header.canToggleDrivingInput = false;
			header.setDrivingInput(false);
			addInput(header);
			headers.add(header);
		}
	}

	@Override
	public void sendOutput() {
		List<String> errors = new LinkedList<>();

		// get from server either JSON object with values for named outputs, or list of output values
		List<Object> values = new LinkedList<>();
		JSONObject result = null;
		try {
			// build and prepare the HttpRequest: inputs are added to URL parameter string if body is not available
			HttpRequestBase request;
			switch (verb.getValue()) {
				case "POST":
				case "PUT":
				case "PATCH":
					Map<String, Object> bodyMap = new HashMap<>();
					for (Input in : httpInputs) {
						String inputName = in.getDisplayName();
						if (inputName == null) { inputName = in.getName(); }
						bodyMap.put(inputName, in.getValue());
					}
					String bodyString = new JSONObject(bodyMap).toString();
					HttpEntityEnclosingRequestBase r =
							verb.getValue().equalsIgnoreCase("POST") ? new HttpPost(URL.getValue()) :
							verb.getValue().equalsIgnoreCase("PUT")  ? new HttpPut(URL.getValue()) :
																	   new HttpPatch(URL.getValue());
					r.setEntity(new StringEntity(bodyString));
					request = r;
					break;
				case "GET":
				case "DELETE":
					List<NameValuePair> params = new LinkedList<>();
					for (Input in : httpInputs) {
						String inputName = in.getDisplayName();
						if (inputName == null) { inputName = in.getName(); }
						params.add(new BasicNameValuePair(inputName, in.getValue().toString()));
					}
					String url = URL.getValue() + "?" + URLEncodedUtils.format(params, "UTF-8");
					request = verb.getValue().equalsIgnoreCase("GET") ? new HttpGet(url) : new HttpDelete(url);
					break;
				default:
					throw new RuntimeException("Unexpected " + verb);
			}

			for (StringParameter header : headers) {
				String headerName = header.getDisplayName();
				if (headerName == null) { headerName = header.getName(); }
				request.addHeader(headerName, header.getValue());
			}

			// send off the HttpRequest to server
			long startTime = System.currentTimeMillis();
			HttpResponse httpResponse = getHttpClient().execute(request);
			log.info("HTTP request took " + (System.currentTimeMillis() - startTime) + " ms");

			// parse response into a JSONObject (or simple list of values)
			String responseString = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			JSONTokener parser = new JSONTokener(responseString);
			Object response = parser.nextValue();
			if (response instanceof JSONObject) {
				// TODO: http://jsonapi.org/format/ suggests we should read "data" member if available
				result = (JSONObject)response;
			} else if (response instanceof JSONArray) {
				// array => send first-found object to outputs, or send one primitive for each output if found first
				result = findJSONObjectFromJSONArray((JSONArray)response, values, httpOutputs.size());
			} else {
				// primitive => send values to outputs as-is
				values.add(response);
				while (parser.more() && values.size() < httpOutputs.size()) {
					values.add(parser.nextValue());
				}
			}
		} catch (RuntimeException | IOException e) {
			errors.add(e.getMessage());
			e.printStackTrace();
		}

		// send results to outputs, match output names to result object keys (ignore extra result object members)
		if (result != null) {
			for (Output out : httpOutputs) {
				String key = out.getDisplayName();
				Object value = MapTraversal.getProperty(result, key);
				if (value != null) {
					out.send(value);
				} else {
					errors.add(key + " not found (or was null) in HTTP response!");
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

		if (!errors.isEmpty()) {
			this.errorOut.send(StringUtils.join(errors, "\n"));
		}
	}

	/**
	 * Walk arrays, grab first found object; or collect given number of primitives
	 * @param primitives that were found IF return value is null. If return value is not null, primitives is invalid.
	 * @return JSONObject that was first found, or null if $maxCount primitives were found first
	 */
	private JSONObject findJSONObjectFromJSONArray(JSONArray arr, List<Object> primitives, int maxCount) {
		for (int i = 0; i < arr.length() && primitives.size() < maxCount; i++) {
			Object ob = arr.opt(i);
			if (ob instanceof JSONArray) {
				ob = findJSONObjectFromJSONArray((JSONArray)ob, primitives, maxCount);
			} else {
				primitives.add(ob);
			}

			if (ob instanceof JSONObject) {
				return (JSONObject)ob;
			}
		}
		return null;
	}

	@Override
	public void clearState() {}

	private static class HttpVerbParameter extends StringParameter {
		public HttpVerbParameter(AbstractSignalPathModule owner, String name) {
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
	}
}
