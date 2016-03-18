package com.unifina.signalpath.remote;

import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.BaseRequest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.unifina.signalpath.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class Http extends AbstractSignalPathModule {

	private HttpVerbParameter verb = new HttpVerbParameter(this, "verb");
	private StringParameter URL = new StringParameter(this, "URL", "http://localhost");

	// subset of inputs/outputs that correponds to HTTP parameters and response data
	private List<Input<Object>> httpInputs;
	private List<Output<Object>> httpOutputs;

	private StringOutput errorOut = new StringOutput(this, "error");
	private Input<Object> trigger = new Input<>(this, "trigger", "Object");

	private static final Logger log = Logger.getLogger(Http.class);

	@Override
	public void init() {
		addInput(verb);
		addInput(URL);
		addInput(trigger);
		trigger.canToggleDrivingInput = false;
		trigger.setDrivingInput(true);
		addOutput(errorOut);
	}

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
				)
			)
		);
		return config;
	}

	@Override
	public void onConfiguration(Map<String,Object> config) {
		super.onConfiguration(config);

		Map options = (Map)config.get("options");
		int inputCount = (options == null) ? 0 : (int)((Map)options.get("inputCount")).get("value");
		int outputCount = (options == null) ? 0 : (int)((Map)options.get("outputCount")).get("value");

		httpInputs = new ArrayList<Input<Object>>(inputCount);
		for (int i = 0; i < inputCount; i++) {
			Input<Object> in = new Input<>(this, "in"+(i+1), "Object");
			addInput(in);
			httpInputs.add(in);
		}
		httpOutputs = new ArrayList<Output<Object>>(outputCount);
		for (int i = 0; i < outputCount; i++) {
			Output<Object> out = new Output<>(this, "out"+(i+1), "Object");
			addOutput(out);
			httpOutputs.add(out);
		}

	}

	@Override
	public void sendOutput() {
		List<String> errors = new LinkedList<>();
		try {
			/*SSLContext sslcontext = SSLContexts.custom()
				.loadTrustMaterial(null, new TrustSelfSignedStrategy())
				.build();

			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext);

			CloseableHttpClient httpclient = HttpClients.custom()
				.setSSLSocketFactory(sslsf)
				.build();
			Unirest.setHttpClient(httpclient);
			*/
			//Unirest.setTimeouts(4000, 4000);

			// build and prepare the HttpRequest
			HttpRequest request;
			switch (verb.getValue()) {
				case "POST":
					request = Unirest.post(URL.getValue());
					Map<String, Object> bodyMap = new HashMap<>();
					for (Input in : httpInputs) {
						bodyMap.put(in.getDisplayName(), in.getValue());
					}
					((HttpRequestWithBody)request).body(new JSONObject(bodyMap).toString());
					break;
				case "GET": {
					request = Unirest.get(URL.getValue());
					break;
				}
				default:
					throw new RuntimeException("Unexpected " + verb);
			}
			//request.header("X-UUID", UUID)

			// send off the HttpRequest to server
			long startTime = System.currentTimeMillis();
			HttpResponse<JsonNode> response = request.asJson();
			log.info("HttpRequest took " + (System.currentTimeMillis() - startTime) + " ms");

			// parse response into a JSONObject that can be sent forward
			JSONObject result = null;
			if (response.getBody().isArray()) {
				JSONArray arr = response.getBody().getArray();
				List<Object> values = new LinkedList<>();
				result = findJSONObjectFromJSONArray(arr, values, httpOutputs.size());
				if (result == null) {
					// ...or just send the first |httpOutputs| found values
					Iterator<Object> iV = values.iterator();
					Iterator<Output<Object>> iO = httpOutputs.iterator();
					while (iV.hasNext() && iO.hasNext()) {
						iO.next().send(iV.next());
					}
				}
			} else {
				result = response.getBody().getObject();
			}

			// send results through corresponding outputs (ignore extra)
			if (result != null) {
				for (Output out : httpOutputs) {
					String key = out.getDisplayName();
					if (result.has(key)) {
						Object value = result.opt(key);
						out.send(value);
					} else {
						errors.add(key + " not found in HTTP response!");
					}
				}
			}

		} catch (UnirestException e) {
			errors.add(StringUtils.join(e.getStackTrace(), '\n'));
			e.printStackTrace();
		} /*catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}*/ catch (RuntimeException e) {
			errors.add(e.getMessage());
			e.printStackTrace();
		}

		if (!errors.isEmpty()) {
			this.errorOut.send(StringUtils.join(errors, "\n"));
		}
	}

	/**
	 * Walk arrays, grab first found object; or collect primitives as many as there are outputs
	 * @return JSONObject that was first found, or null if found one primitive for each output first
	 */
	private JSONObject findJSONObjectFromJSONArray(JSONArray arr, List<Object> primitives, int maxCount) {
		for (int i = 0; i < arr.length(); i++) {
			Object ob = arr.opt(i);
			if (ob instanceof JSONArray) {
				return findJSONObjectFromJSONArray((JSONArray)ob, primitives, maxCount);
			} else if (ob instanceof JSONObject) {
				return (JSONObject)ob;
			} else {
				primitives.add(arr);
				if (primitives.size() >= maxCount) { break; }
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
				new PossibleValue("POST", "POST")
			);
		}
		@Override public Map<String, Object> getConfiguration() {
			Map<String, Object> config = super.getConfiguration();
			config.put("possibleValues", getValueList());
			return config;
		}
	}
}
