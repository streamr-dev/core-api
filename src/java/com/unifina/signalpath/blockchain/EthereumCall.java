package com.unifina.signalpath.blockchain;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.ListOutput;
import com.unifina.signalpath.StringOutput;
import com.unifina.signalpath.StringParameter;
import com.unifina.signalpath.remote.AbstractHttpModule;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Send out a call to specified function in Ethereum block chain
 */
public class EthereumCall extends AbstractHttpModule {

	public static final String ETH_WRAPPER_URL = "http://localhost:3000/call";
	private static final Logger log = Logger.getLogger(EthereumCall.class);

	private StringParameter abiString = new StringParameter(this, "interface", "");
	private StringParameter address = new StringParameter(this, "address", "");
	private StringParameter function = new StringParameter(this, "function", "");	// TODO: change into drop-down list
	private ListOutput errors = new ListOutput(this, "errors");

	private StringOutput result = new StringOutput(this, "result");

	private List<Input<Object>> arguments = new ArrayList<>();

	// TODO: proper check if param changed
	//private String currentAbiString = "";
	//private String currentFunctionString = "";

	private Function chosenFunction;
	private boolean isValid = false;

	private JsonElement abi;

	private static class Slot {
		String name;
		String type;
	}

	private static class Function {
		String name;
		List<Slot> inputs;
		List<Slot> outputs;
		Boolean payable;
		Boolean constant;
	}

	private List<Function> functions;

	@Override
	public void init() {
		addInput(abiString);
		addInput(address);
		addInput(function);
		abiString.setUpdateOnChange(true);		// update function list parameter
		function.setUpdateOnChange(true);		// update argument inputs

		addOutput(result);
		addOutput(errors);
	}

	@Override
	public void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		// TODO: proper check if param changed
		//if (!currentAbiString.equals(abiString.getValue())) {
			String currentAbiString = abiString.getValue();
			isValid = false;

			log.info("Parsing interface: " + currentAbiString);
			abi = new JsonParser().parse(currentAbiString);

			// re-read function arguments
			//chosenFunction = null;
			//currentFunctionString = "";

			functions = new ArrayList<>();
			for (JsonElement e : abi.getAsJsonArray()) {
				JsonObject member = e.getAsJsonObject();
				if (member.get("type").getAsString().equals("function")) {
					Function f = new Gson().fromJson(member, Function.class);
					log.info("Found function " + f.name + (f.constant ? " [constant]" : "") + (f.payable ? " [payable]" : ""));
					functions.add(f);
				}
			}

			// TODO: update ContractFunctionParameter
		//}

		// TODO: proper check if param changed
		//if (!currentFunctionString.equals(function.getValue())) {
			String currentFunctionString = function.getValue();

			Function newF = null;
			for (Function f : functions) {
				if (f.name.equals(currentFunctionString)) {
					newF = f;
					break;
				}
			}
			if (newF != null) {
				chosenFunction = newF;
				log.info("Chose function " + chosenFunction.name);
			} else {
				log.info("Can't find " + currentFunctionString);
			}

			if (chosenFunction != null) {
				arguments = new ArrayList<>();
				for (Slot s : chosenFunction.inputs) {
					String name = s.name;
					String type = ethToStreamrType(s.type);
					Input<Object> input = new Input<>(this, name, type);
					addInput(input);
					arguments.add(input);
				}
				isValid = true;
			}
		//}
	}

	/**
	 * Maps Ethereum types to Streamr types
	 * @param ethType
	 * @return Streamr type: "Double" for numbers, "String" for addresses/strings, otherwise "Object"
     */
	public static String ethToStreamrType(String ethType) {
		return  ethType.equals("address") ? "String" :
				ethType.equals("string") ? "String" :
				ethType.substring(0, 5).equals("bytes") ? "String" :
				ethType.substring(0, 4).equals("uint") ? "Double" :
				ethType.substring(0, 3).equals("int") ? "Double" : "Object";
	}

	@Override
	public void clearState() {
		//currentAbiString = "";
		//currentFunctionString = "";
		chosenFunction = null;
		isValid = false;
	}

	/**
	 * Prepare HTTP request based on module inputs
	 * @return HTTP request that will be sent to server
	 */
	@Override
	protected HttpRequestBase createRequest() {
		if (!isValid) {
			throw new RuntimeException("Need valid address to call!");
		}

		String sourceAddress = "0xb3428050ea2448ed2e4409be47e1a50ebac0b2d2";
		String targetAddress = address.getValue();
		String functionName = chosenFunction.name;

		List<JsonPrimitive> argList = new ArrayList<>();
		for (Input<Object> input : arguments) {
			String name = input.getName();
			String value = input.getValue().toString();
			log.info("  " + name + ": " + value);
			argList.add(new JsonPrimitive(value));
		}

		Map args = ImmutableMap.of(
				"source", sourceAddress,
				"target", targetAddress,
				"function", functionName,
				"arguments", argList,
				"abi", abi
		);
		String jsonString = new Gson().toJson(args);

		HttpPost request = new HttpPost(ETH_WRAPPER_URL);
		try {
			log.info("Sending function call: " + jsonString);
			request.setEntity(new StringEntity(jsonString));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return request;
	}

	private static class EthWrapperResponse {
		String result;
	}

	@Override
	public void sendOutput(HttpTransaction call) {

		try {
			if (call.response == null) { throw new RuntimeException("No response from server"); }

			HttpEntity entity = call.response.getEntity();
			if (entity == null) { throw new RuntimeException("Empty response from server"); }

			String responseString = EntityUtils.toString(entity, "UTF-8");
			if (responseString.isEmpty()) { throw new RuntimeException("Empty response from server"); }

			EthWrapperResponse resp = new Gson().fromJson(responseString, EthWrapperResponse.class);

			result.send(resp.result);

		} catch (Exception e) {
			call.errors.add(e.getMessage());
		}

		if (call.errors.size() > 0) {
			errors.send(call.errors);
		}
	}

}
