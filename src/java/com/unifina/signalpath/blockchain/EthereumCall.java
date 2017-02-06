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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Send out a call to specified function in Ethereum block chain
 */
public class EthereumCall extends AbstractHttpModule {

	public static final String ETH_WRAPPER_URL = "http://localhost:3000/call";
	private static final Logger log = Logger.getLogger(EthereumCall.class);

	private transient Gson gson; // not guaranteed thread-safe

	private EthereumContractInput contract = new EthereumContractInput(this, "contract");

	private ListOutput errors = new ListOutput(this, "errors");
	private TimeSeriesInput ether = new TimeSeriesInput(this, "ether");
	private Input<Object> trigger = new Input<>(this, "trigger", "Object");		// shown if there are no inputs

	private List<EthereumABI.Function> contractFunctions = new ArrayList<>();
	private List<EthereumABI.Event> contractEvents = new ArrayList<>();
	private EthereumABI.Function chosenFunction;
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

	@Override
	public void init() {
		addInput(contract);
		function.setUpdateOnChange(true);		// update argument inputs

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

		// Find the function input config and configure it manually.
		// The input is not yet configured, so otherwise it won't hold the correct value yet.
		if (config.containsKey("params")) {
			for (Object inputConfig : MapTraversal.getList(config, "params")) {
				if (MapTraversal.getString((Map) inputConfig, "name").equals(function.getName())) {
					function.setConfiguration((Map<String, Object>) inputConfig);
					break;
				}
			}
			updateFunction();
		}
	}

	private void updateInterface() {
		isValid = false;
		//currentFunctionString = ""; // force re-read function arguments (maybe signature changed in abi)

		contractFunctions.clear();	// don't re-create; the reference is used in FunctionNameParameter
		contractEvents.clear();

		if (contract.hasValue()) {
			EthereumABI abi = contract.getValue().getABI();

			for (EthereumABI.Function f : abi.getFunctions()) {
				contractFunctions.add(f);
				// TODO: move function input creation here (like events below)
			}

			for (EthereumABI.Event ev : abi.getEvents()) {
				contractEvents.add(ev);
				ev.outputs = new ArrayList<>();
				if (ev.inputs.size() > 0) {
					for (EthereumABI.Slot arg : ev.inputs) {
						String displayName = ev.name + "." + (arg.name.length() > 0 ? arg.name : "(" + arg.type + ")");
						Output output = EthereumToStreamrTypes.asOutput(arg.type, displayName, this);
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

	private void updateFunction() {
		if (contractFunctions.size() < 1) { return; }

		addInput(function);
		chosenFunction = function.getSelected();
		if (chosenFunction == null) {
			log.warn("Can't find function" + function.getValue());
			chosenFunction = contractFunctions.get(0);
			function.receive(chosenFunction.name);
			//return;		// remain !isValid
		}
		log.info("Chose function " + chosenFunction.name);

		arguments.clear();
		for (EthereumABI.Slot s : chosenFunction.inputs) {
			String name = s.name;
			if (name.length() < 1) { name = "(" + s.type + ")"; }
			Input input = EthereumToStreamrTypes.asInput(s.type, name, this);
			addInput(input);
			arguments.add(input);
		}
		if (chosenFunction.inputs.size() == 0) {
			addInput(trigger);
		}

		if (chosenFunction.constant) {
			// constant functions send an eth_call and get back returned result(s)
			results.clear();
			for (EthereumABI.Slot s : chosenFunction.outputs) {
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
			for (EthereumABI.Event ev : contractEvents) {
				for (Output<Object> output : ev.outputs) {
					addOutput(output);
				}
			}
		}

		isValid = true;
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
		args.put("target", contract.getValue().getAddress());
		args.put("function", chosenFunction.name);

		List<JsonPrimitive> argList = new ArrayList<>();
		for (Input<Object> input : arguments) {
			String name = input.getName();
			String value = input.getValue().toString();
			log.info("  " + name + ": " + value);
			argList.add(new JsonPrimitive(value));
		}
		args.put("arguments", argList);
		args.put("abi", contract.getValue().getABI().toList());

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
				for (EthereumABI.Event event : contractEvents) {
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
		private List<EthereumABI.Function> functionList;
		public FunctionNameParameter(AbstractSignalPathModule owner, String name, List<EthereumABI.Function> functions) {
			super(owner, name, "");
			functionList = functions;
		}

		@Override
		protected List<PossibleValue> getPossibleValues() {
			List<PossibleValue> ret = new ArrayList<>();
			for (EthereumABI.Function f : functionList) {
				ret.add(new PossibleValue(f.name, f.name));
			}
			return ret;
		}

		public EthereumABI.Function getSelected() {
			String v = this.getValue();
			for (EthereumABI.Function f : functionList) {
				if (f.name.equals(v)) {
					return f;
				}
			}
			return null;
		}
	}
}
