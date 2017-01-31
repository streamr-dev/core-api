package com.unifina.signalpath.blockchain;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.unifina.signalpath.*;
import com.unifina.signalpath.remote.AbstractHttpModule;
import com.unifina.utils.MapTraversal;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.Serializable;
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
	private static transient Gson gson;

	private StringParameter abiString = new StringParameter(this, "interface", "");
	private StringParameter address = new StringParameter(this, "address", "");
	private ListOutput errors = new ListOutput(this, "errors");

	private List<Function> functions = new ArrayList<>();
	private FunctionNameParameter function = new FunctionNameParameter(this, "function", functions);
	private List<Input<Object>> arguments = new ArrayList<>();

	// TODO: is this proper way to check if param changed?
	private String currentAbiString = "";
	//private String currentFunctionString = "";

	private transient JsonElement abi;
	private Function chosenFunction;
	private boolean isValid = false;

	private Output<Object> result;
	private TimeSeriesOutput valueSent = new TimeSeriesOutput(this, "valueSent");
	private TimeSeriesOutput valueReceived = new TimeSeriesOutput(this, "valueReceived");
	private TimeSeriesOutput gasUsed = new TimeSeriesOutput(this, "gasUsed");
	private TimeSeriesOutput gasPrice = new TimeSeriesOutput(this, "gasPrice");
	private TimeSeriesOutput blockNumber = new TimeSeriesOutput(this, "blockNumber");
	private TimeSeriesOutput nonce = new TimeSeriesOutput(this, "nonce");

	/** Ethereum contract input/output */
	public static class Slot implements Serializable {
		String name;
		String type;
	}

	/** Ethereum contract member function */
	public static class Function implements Serializable {
		String name;
		List<Slot> inputs;
		List<Slot> outputs;
		Boolean payable;
		Boolean constant;
	}

	@Override
	public void init() {
		addInput(address);
		addInput(abiString);

		abiString.setUpdateOnChange(true);		// update function list parameter
		function.setUpdateOnChange(true);		// update argument inputs

		// hack to work around: onConfiguration-inserted param can't receive properly value from param change
//		if (functions.size() > 0) {
//			addInput(function);
//		}

		addOutput(errors);
	}

	@Override
	public void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		updateInterface();
		// hack to work around: onConfiguration-inserted param can't receive properly value from param change
		String functionName = MapTraversal.getString(config, "params[2].value", "");
		updateFunction(functionName);
	}

	private void updateInterface() {
		if (currentAbiString.equals(abiString.getValue())) { return; }
		String currentAbiString = abiString.getValue();
		isValid = false;
		if (gson == null) { gson = new Gson(); }
		//currentFunctionString = ""; // force re-read function arguments (maybe signature changed in abi)

		log.info("Parsing interface: " + currentAbiString);
		abi = new JsonParser().parse(currentAbiString);

		functions.clear();	// don't re-create; the reference is used in FunctionNameParameter
		for (JsonElement e : abi.getAsJsonArray()) {
			JsonObject member = e.getAsJsonObject();
			if (member.get("type").getAsString().equals("function")) {
				Function f = gson.fromJson(member, Function.class);
				log.info("Found function " + f.name + (f.constant ? " [constant]" : "") + (f.payable ? " [payable]" : ""));
				functions.add(f);
			}
		}
	}

	private void updateFunction(String functionName) {
		if (functions.size() < 1) { return; }

		addInput(function);
		function.receive(functionName); // hack to work around onConfiguration-inserted param bug
		chosenFunction = function.getSelected();
		if (chosenFunction == null) {
			log.info("Can't find " + function.getValue());
			chosenFunction = functions.get(0);
			function.receive(chosenFunction.name);
			//return;		// remain !isValid
		}
		log.info("Chose function " + chosenFunction.name);

		arguments.clear();
		for (Slot s : chosenFunction.inputs) {
			String name = s.name;
			String type = ethToStreamrType(s.type);
			if ("".equals(name)) { name = "key"; }	// public mapping getters
			Input<Object> input = new Input<>(this, name, type);
			addInput(input);
			arguments.add(input);
		}
		isValid = true;

		if (chosenFunction.constant) {
			// constant functions send an eth_call and get back returned result
			if (chosenFunction.outputs.size() > 0) {
				Slot out = chosenFunction.outputs.get(0);
				//String type = ethToStreamrType(out.type);
				//result = new Output<>(this, "result", type);	// outputs' name in ABI is always ""
				result = new Output<>(this, "result", "String");
				addOutput(result);
			}
		} else {
			// non-constant functions modify contract state,
			// 	 and thus require a transaction to be written into blockchain block (eth_sendTransaction)
			// this takes time (module is async) and result can NOT be returned,
			//   instead return info about the transaction
			addOutput(valueSent);
			addOutput(valueReceived);
			addOutput(gasUsed);
			addOutput(gasPrice);
			addOutput(blockNumber);
			addOutput(nonce);
		}
	}


	/**
	 * Maps Ethereum types to Streamr types
	 * NB: obviously casting e.g. uint256 to Double loses precision (range should be sufficient), so
	 * 		this can be a VERY bad idea for e.g. financial transactions! Streamr needs BigInteger support for that.
	 * 	Not handled: fixed-size arrays (int[3]), dynamic-size arrays (uint[]), they will be "Object"
	 * @param ethType
	 * @return Streamr type: "Double" for numbers, "String" for addresses/strings, "Boolean" for bool, otherwise "Object"
     */
	public static String ethToStreamrType(String ethType) {
		return  ethType.equals("address") ? "String" :			// 20 bytes
				ethType.equals("string") ? "String" :			// variable
				ethType.startsWith("function") ? "String" :		// 20-byte address + 4-byte selector
				ethType.equals("bool") ? "Boolean" :			// 1 byte
				ethType.startsWith("bytes") ? "String" :		// variable
				ethType.startsWith("fixed") ? "Double" :		// post-fixed with bit width
				ethType.startsWith("ufixed") ? "Double" :		// post-fixed with bit width
				ethType.startsWith("uint") ? "Double" :			// post-fixed with bit width
				ethType.startsWith("int") ? "Double" :			// post-fixed with bit width
						"Object";
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
			throw new RuntimeException("Need valid function to call!");
		}

		//String sourceAddress = "0xb3428050ea2448ed2e4409be47e1a50ebac0b2d2";
		String targetAddress = address.getValue();
		String functionName = chosenFunction.name;

		List<JsonPrimitive> argList = new ArrayList<>();
		for (Input<Object> input : arguments) {
			String name = input.getName();
			String value = input.getValue().toString();
			log.info("  " + name + ": " + value);
			argList.add(new JsonPrimitive(value));
		}

		if (abi == null) { abi = new JsonParser().parse(currentAbiString); }
		Map args = ImmutableMap.of(
				"target", targetAddress,
				"function", functionName,
				"arguments", argList,
				"abi", abi
		);
		if (gson == null) { gson = new Gson(); }
		String jsonString = gson.toJson(args);

		HttpPost request = new HttpPost(ETH_WRAPPER_URL);
		try {
			log.info("Sending function call: " + jsonString);
			request.setEntity(new StringEntity(jsonString));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return request;
	}

	public static class Response {
		public String error;
	}
	public static class CallResponse extends Response {
		public String result;
	}
	public static class TransactionResponse extends Response {
		public Double valueSent;
		public Double valueReceived;
		public Double gasUsed;
		public Double gasPrice;
		public Double blockNumber;
		public Double nonce;
	}

	@Override
	public void sendOutput(HttpTransaction call) {
		try {
			if (call.response == null) { throw new RuntimeException("No response from server"); }

			HttpEntity entity = call.response.getEntity();
			if (entity == null) { throw new RuntimeException("Empty response from server"); }

			String responseString = EntityUtils.toString(entity, "UTF-8");
			if (responseString.isEmpty()) { throw new RuntimeException("Empty response from server"); }

			if (gson == null) { gson = new Gson(); }
			Response r = gson.fromJson(responseString, Response.class);
			if (r.error != null) { throw new RuntimeException(r.error); }

			if (chosenFunction.constant) {
				// TODO: cast result to correct type
				CallResponse resp = gson.fromJson(responseString, CallResponse.class);
				result.send(resp.result);
			} else {
				TransactionResponse resp = gson.fromJson(responseString, TransactionResponse.class);
				valueSent.send(resp.valueSent);
				valueReceived.send(resp.valueReceived);
				gasUsed.send(resp.gasUsed);
				gasPrice.send(resp.gasPrice);
				blockNumber.send(resp.blockNumber);
				nonce.send(resp.nonce);
			}
		} catch (Exception e) {
			call.errors.add(e.getMessage());
		}

		if (call.errors.size() > 0) {
			errors.send(call.errors);
		}
	}

	public static class FunctionNameParameter extends StringParameter {
		private List<Function> functionList;
		public FunctionNameParameter(AbstractSignalPathModule owner, String name, List<Function> functions) {
			super(owner, name, "");
			functionList = functions;
		}

		@Override
		protected List<PossibleValue> getPossibleValues() {
			List<PossibleValue> ret = new ArrayList<>();
			for (Function f : functionList) {
				ret.add(new PossibleValue(f.name, f.name));
			}
			return ret;
		}

		public Function getSelected() {
			String v = this.getValue();
			for (Function f : functionList) {
				if (f.name.equals(v)) {
					return f;
				}
			}
			return null;
		}
	}
}
