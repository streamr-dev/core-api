package com.unifina.signalpath.remote;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.unifina.signalpath.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import java.util.*;

public class Http extends AbstractSignalPathModule {

	private int inputCount = 0;
	private int outputCount = 0;

	private HttpVerbParameter verb = new HttpVerbParameter(this, "verb");
	private StringParameter URL = new StringParameter(this, "URL", "http://localhost");

	private List<Input<Object>> inputs;
	private List<Output<Object>> outputs;

	private StringOutput errorOut = new StringOutput(this, "error");

	private static final Logger log = Logger.getLogger(Http.class);

	@Override
	public void init() {
		addInput(verb);
		addInput(URL);
		addOutput(errorOut);
	}

	@Override
	public Map<String,Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		config.put(
			"options", ImmutableMap.of(
				"inputCount", ImmutableMap.of(
					"value", inputCount,
					"type", "int"
				),
				"outputCount", ImmutableMap.of(
					"value", outputCount,
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

		if (options != null) {
			inputCount = (int)((Map)options.get("inputCount")).get("value");
			outputCount = (int)((Map)options.get("outputCount")).get("value");
		}

		inputs = new ArrayList<Input<Object>>(inputCount);
		for (int i = 0; i < inputCount; i++) {
			Input<Object> in = new Input<>(this, "in"+(i+1), "Object");
			addInput(in);
		}
		outputs = new ArrayList<Output<Object>>(outputCount);
		for (int i = 0; i < outputCount; i++) {
			Output<Object> out = new Output<>(this, "out"+(i+1), "Object");
			addOutput(out);
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

			HttpRequestWithBody request = Unirest.post(URL.getValue());
			Map<String, Object> body = new HashMap<>();
			for (Input in : getInputs()) {
				body.put(in.getDisplayName(), in.getValue());
			}
			request.body(new JSONObject(body).toString());
			//request.header("X-UUID", UUID)

			long startTime = System.currentTimeMillis();
			HttpResponse<JsonNode> response = request.asJson();
			log.info("HttpRequest took " + (System.currentTimeMillis() - startTime) + " ms");

			// parse results into outputs
			JSONObject responseJson = response.getBody().getObject();
			for (Output out : getOutputs()) {
				String key = out.getDisplayName();
				try {
					Object value = responseJson.get(key);
					out.send(value);
				} catch (JSONException e) {
					errors.add(key + " not found in HTTP response!");
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
		}*/
		if (!errors.isEmpty()) {
			this.errorOut.send(StringUtils.join(errors, "\n"));
		}
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
