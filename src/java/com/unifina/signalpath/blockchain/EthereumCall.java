package com.unifina.signalpath.blockchain;

import com.google.gson.*;
import com.unifina.signalpath.*;
import com.unifina.signalpath.remote.AbstractHttpModule;
import com.unifina.utils.MapTraversal;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

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
	private TimeSeriesInput ether = new TimeSeriesInput(this, "ether");
	private Input<Object> trigger = new Input<>(this, "trigger", "Object");		// shown if there are no inputs

	// TODO: is this proper way to check if param changed?
	private String currentAbiString = "";
	//private String currentFunctionString = "";

	private transient JsonElement abi;
	private List<Function> contractFunctions = new ArrayList<>();
	private List<Event> contractEvents = new ArrayList<>();
	private Function chosenFunction;
	private boolean isValid = false;

	private FunctionNameParameter function = new FunctionNameParameter(this, "function", contractFunctions);
	private List<Input<Object>> arguments = new ArrayList<>();
	private List<Output<Object>> results = new ArrayList<>();
	private List<Output<Object>> events = new ArrayList<>();

	private TimeSeriesOutput valueSent = new TimeSeriesOutput(this, "valueSent");
	private TimeSeriesOutput valueReceived = new TimeSeriesOutput(this, "valueReceived");
	private TimeSeriesOutput gasUsed = new TimeSeriesOutput(this, "gasUsed");
	private TimeSeriesOutput gasPrice = new TimeSeriesOutput(this, "gasPrice");
	private TimeSeriesOutput blockNumber = new TimeSeriesOutput(this, "blockNumber");
	private TimeSeriesOutput nonce = new TimeSeriesOutput(this, "nonce");

	/** Ethereum contract input/output */
	public static class Slot implements Serializable {
		public String name;
		public String type;
	}

	/** Ethereum contract member function */
	public static class Function implements Serializable {
		public String name;
		public List<Slot> inputs;
		public List<Slot> outputs;
		public Boolean payable;
		public Boolean constant;
	}

	public static class Event implements Serializable {
		public String name;
		public List<Slot> inputs;				// "inputs" that receive value from Solidity contract
		public List<Output<Object>> outputs;	// "outputs" from Streamr module on canvas
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

		trigger.canToggleDrivingInput = false;
		trigger.setDrivingInput(true);
		trigger.requiresConnection = false;
		ether.requiresConnection = false;

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
		//currentFunctionString = ""; // force re-read function arguments (maybe signature changed in abi)

		log.info("Parsing interface: " + currentAbiString);
		abi = new JsonParser().parse(currentAbiString);

		if (gson == null) { gson = new Gson(); }
		contractFunctions.clear();	// don't re-create; the reference is used in FunctionNameParameter
		contractEvents.clear();
		for (JsonElement e : abi.getAsJsonArray()) {
			JsonObject member = e.getAsJsonObject();
			String ethType = member.get("type").getAsString();
			if (ethType.equals("function")) {
				Function f = gson.fromJson(member, Function.class);
				log.info("Found function " + f.name + (f.constant ? " [constant]" : "") + (f.payable ? " [payable]" : ""));
				// TODO: move function input creation here (like events below)
				contractFunctions.add(f);
			} else if (ethType.equals("event")) {
				Event ev = gson.fromJson(member, Event.class);
				log.info("Found event " + ev.name);
				contractEvents.add(ev);
				ev.outputs = new ArrayList<>();
				if (ev.inputs.size() > 0) {
					for (Slot arg : ev.inputs) {
						String displayName = ev.name + "." + (arg.name.length() > 0 ? arg.name : "(" + arg.type + ")");
						// TODO: use ethToStreamrType(arg.type) for output
						Output<Object> output = new Output<>(this, displayName, "Object");
						ev.outputs.add(output);
					}
				} else {
					// event without parameters/arguments/inputs
					Output<Object> output = new Output<>(this, ev.name, "Object");
					ev.outputs.add(output);
				}
			}
		}
	}

	private void updateFunction(String functionName) {
		if (contractFunctions.size() < 1) { return; }

		addInput(function);
		function.receive(functionName); // hack to work around onConfiguration-inserted param bug
		chosenFunction = function.getSelected();
		if (chosenFunction == null) {
			log.info("Can't find " + function.getValue());
			chosenFunction = contractFunctions.get(0);
			function.receive(chosenFunction.name);
			//return;		// remain !isValid
		}
		log.info("Chose function " + chosenFunction.name);

		arguments.clear();
		for (Slot s : chosenFunction.inputs) {
			String name = s.name;
			String type = ethToStreamrType(s.type);
			if (name.length() < 1) { name = "(" + s.type + ")"; }
			Input<Object> input = new Input<>(this, name, type);
			addInput(input);
			arguments.add(input);
		}
		if (chosenFunction.inputs.size() == 0) {
			addInput(trigger);
		}

		if (chosenFunction.constant) {
			// constant functions send an eth_call and get back returned result(s)
			results.clear();
			for (Slot s : chosenFunction.outputs) {
				String name = s.name;
				String type = "String"; //ethToStreamrType(s.type);		// TODO: support other return types
				if (name.length() < 1) { name = "(" + s.type + ")"; }
				Output<Object> output = new Output<>(this, name, type);
				addOutput(output);
				results.add(output);
			}
		} else {
			addInput(ether);
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
			// TODO: any way to find out which events can be raised from which functions? (probably not...)
			for (Event ev : contractEvents) {
				for (Output<Object> output : ev.outputs) {
					addOutput(output);
				}
			}
		}

		isValid = true;
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
		Map args = new HashMap<>();

		// TODO: source address should be Ethereum account of the current user
		args.put("source", "0xb3428050ea2448ed2e4409be47e1a50ebac0b2d2");
		args.put("target", address.getValue());
		args.put("function", chosenFunction.name);

		List<JsonPrimitive> argList = new ArrayList<>();
		for (Input<Object> input : arguments) {
			String name = input.getName();
			String value = input.getValue().toString();
			log.info("  " + name + ": " + value);
			argList.add(new JsonPrimitive(value));
		}
		args.put("arguments", argList);

		if (abi == null) { abi = new JsonParser().parse(currentAbiString); }
		args.put("abi", abi);

		if (!chosenFunction.constant) {
			if (ether.isConnected()) {
				BigInteger valueWei = BigDecimal.valueOf(ether.getValue() * Math.pow(10, 18)).toBigInteger();
				args.put("value", valueWei.toString());
			}
			// args.put("returnEvents", eventArguments.exist(o => o.isConnected()))
//			boolean returnEvents = false;
//			for (Output<Object> output : events) {
//				if (output.isConnected()) { returnEvents = true; }
//			}
//			args.put("returnEvents", returnEvents);
		}

		if (gson == null) { gson = new Gson(); }
		String jsonString = gson.toJson(args);

		HttpPost request = new HttpPost(ETH_WRAPPER_URL);
		request.setConfig(RequestConfig.custom()
				.setSocketTimeout(60 * 1000) // 1 minute
				.build());
		try {
			log.info("Sending function call: " + jsonString);
			request.setEntity(new StringEntity(jsonString));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return request;
	}

	public static class TransactionResponse {
		public Map<String, List<Object>> events;		// event name -> args in order they're in ABI
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

			JsonObject response = new JsonParser().parse(responseString).getAsJsonObject();
			if (response.get("error") != null) { throw new RuntimeException(response.get("error").toString()); }

			if (chosenFunction.constant) {
				JsonArray resultValues = response.get("results").getAsJsonArray();
				int n = Math.min(results.size(), resultValues.size());
				for (int i = 0; i < n; i++) {
					// TODO: cast result(s) to correct type(s)
					String result = resultValues.get(i).toString();
					Output<Object> output = results.get(i);
					output.send(result);
				}
			} else {
				if (gson == null) { gson = new Gson(); }
				TransactionResponse resp = gson.fromJson(responseString, TransactionResponse.class);
				valueSent.send(resp.valueSent);
				valueReceived.send(resp.valueReceived);
				gasUsed.send(resp.gasUsed);
				gasPrice.send(resp.gasPrice);
				blockNumber.send(resp.blockNumber);
				nonce.send(resp.nonce);
				for (Event event : contractEvents) {
					List<Object> args = resp.events.get(event.name);
					if (args != null) {
						if (event.inputs.size() > 0) {
							int n = Math.min(event.outputs.size(), args.size());
							for (int i = 0; i < n; i++) {
								// TODO: cast result(s) to correct type(s)
								String value = args.get(i).toString();
								Output<Object> output = event.outputs.get(i);
								output.send(value);
							}
						} else {
							event.outputs.get(0).send(Boolean.TRUE);
						}
					}
				}
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
