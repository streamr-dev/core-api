package com.unifina.signalpath.blockchain;

import com.google.gson.*;
import com.unifina.signalpath.*;
import com.unifina.signalpath.blockchain.templates.EthereumModuleOptions;
import com.unifina.signalpath.remote.AbstractHttpModule;
import com.unifina.utils.MapTraversal;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Send out a call to specified function in Ethereum block chain
 */
public class EthereumCall extends AbstractHttpModule {

	private EthereumModuleOptions ethereumOptions = new EthereumModuleOptions();

	private final EthereumAccountParameter ethereumAccount = new EthereumAccountParameter(this, "ethAccount");
	private EthereumContractInput contract = new EthereumContractInput(this, "contract");

	private ListOutput errors = new ListOutput(this, "errors");
	private TimeSeriesInput ether = new TimeSeriesInput(this, "ether");
	private Input<Object> trigger = new Input<>(this, "trigger", "Object");		// shown if there are no inputs

	private EthereumABI abi;
	private EthereumABI.Function chosenFunction;

	private FunctionNameParameter function = new FunctionNameParameter(this, "function");
	private List<Input<Object>> arguments = new ArrayList<>();

	// constant function outputs
	private List<Output<Object>> results = new ArrayList<>();

	// transaction outputs
	private TimeSeriesOutput valueSent = new TimeSeriesOutput(this, "spentWei");
	private TimeSeriesOutput valueReceived = new TimeSeriesOutput(this, "targetChangeWei");
	private TimeSeriesOutput gasUsed = new TimeSeriesOutput(this, "gasUsed");
	private TimeSeriesOutput gasPrice = new TimeSeriesOutput(this, "gasPriceWei");
	private TimeSeriesOutput blockNumber = new TimeSeriesOutput(this, "blockNumber");
	private TimeSeriesOutput nonce = new TimeSeriesOutput(this, "nonce");
	private StringOutput txHash = new StringOutput(this, "txHash");
	private Map<EthereumABI.Event, List<Output<Object>>> eventOutputs;		// outputs for each event separately

	private static final Logger log = Logger.getLogger(EthereumCall.class);
	private static final int ETHEREUM_TRANSACTION_DEFAULT_TIMEOUT_SECONDS = 1800;

	private transient Gson gson;

	@Override
	public void init() {
		addInput(ethereumAccount);
		addInput(contract);
		contract.setDrivingInput(false);
		contract.canToggleDrivingInput = false;

		function.setUpdateOnChange(true);		// update argument inputs

		trigger.canToggleDrivingInput = false;
		trigger.setDrivingInput(true);
		trigger.requiresConnection = false;
		ether.requiresConnection = false;

		addOutput(errors);

		// by default wait patiently for the next mined block, at least 10 minutes
		timeoutMillis = ETHEREUM_TRANSACTION_DEFAULT_TIMEOUT_SECONDS * 1000;
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions.writeTo(options);

		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions = EthereumModuleOptions.readFrom(options);

		// ethereum RPC is JSON only
		bodyContentType = AbstractHttpModule.BODY_FORMAT_JSON;

		updateInterface();

		// Find the function input config and configure it manually.
		// The input is not yet configured, so otherwise it won't hold the correct value yet.
		if (config.containsKey("params")) {
			for (Object configObject : MapTraversal.getList(config, "params")) {
				Map<String, Object> inputConfig = (Map<String, Object>)configObject;
				if (function.getName().equals(MapTraversal.getString(inputConfig, "name"))) {
					function.setConfiguration(inputConfig);
					break;
				}
			}
			updateFunction();
		}
	}

	private void updateInterface() {
		clearState();

		if (contract.hasValue()) {
			abi = contract.getValue().getABI();
			function.list = abi.getFunctions();

			for (EthereumABI.Event ev : abi.getEvents()) {
				List<Output<Object>> evOutputs = new ArrayList<>();
				if (ev.inputs.size() > 0) {
					for (EthereumABI.Slot arg : ev.inputs) {
						String displayName = ev.name + "." + (arg.name.length() > 0 ? arg.name : "(" + arg.type + ")");
						Output output = EthereumToStreamrTypes.asOutput(arg.type, displayName, this);
						evOutputs.add(output);
					}
				} else {
					// event without parameters/arguments/inputs
					Output output = new BooleanOutput(this, ev.name);
					evOutputs.add(output);
				}
				eventOutputs.put(ev, evOutputs);
			}
		}
	}

	private void updateFunction() {
		if (function.list == null || function.list.size() < 1) { return; }

		addInput(function);
		chosenFunction = function.getSelected();
		if (chosenFunction == null) {
			log.warn("Can't find function " + function.getValue());
			chosenFunction = function.list.get(0);
			function.receive(chosenFunction.name);
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
		if (chosenFunction.inputs.size() == 0 && (chosenFunction.constant || !chosenFunction.payable)) {
			addInput(trigger);
		}

		if (chosenFunction.constant) {
			// constant functions send an eth_call and get back returned result(s)
			results.clear();
			for (EthereumABI.Slot s : chosenFunction.outputs) {
				String name = s.name;
				if (name.length() < 1) { name = "(" + s.type + ")"; }
				Output output = EthereumToStreamrTypes.asOutput(s.type, name, this);
				addOutput(output);
				results.add(output);
			}
		} else {
			if (chosenFunction.payable) {
				addInput(ether);
			}
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
			addOutput(txHash);
			// TODO: any way to find out which events can be raised from chosenFunction? (probably not...)
			for (EthereumABI.Event ev : abi.getEvents()) {
				for (Output<Object> output : eventOutputs.get(ev)) {
					addOutput(output);
				}
			}
		}
	}


	@Override
	public void clearState() {
		abi = null;
		function.list = null;
		eventOutputs = new HashMap<>();
		chosenFunction = null;
	}

	/**
	 * Prepare HTTP request based on module inputs
	 * @return HTTP request that will be sent to server
	 */
	@Override
	protected HttpRequestBase createRequest() {
		if (gson == null) { gson = new Gson(); }
		EthereumContract c = contract.getValue();
		if (c == null || c.getABI() == null || function.list == null) { throw new RuntimeException("Faulty contract"); }
		if (chosenFunction == null) { throw new RuntimeException("Need valid function to call"); }
		if (c.getAddress() == null) { throw new RuntimeException("Contract must be deployed before calling"); }

		Map args = new HashMap<>();
		args.put("source", ethereumAccount.getAddress());
		args.put("key", ethereumAccount.getPrivateKey());
		args.put("gasprice", ethereumOptions.getGasPriceWei());
		args.put("target", c.getAddress());

		HttpPost request = null;
		if (chosenFunction.name.length() > 0) {
			args.put("function", chosenFunction.name);

			List<JsonElement> argList = new ArrayList<>();
			for (Input<Object> input : arguments) {
				String name = input.getName();
				Object value = input.getValue();
				log.info("  " + name + ": " + value.toString());
				argList.add(gson.toJsonTree(value));
			}
			args.put("arguments", argList);
			args.put("abi", c.getABI().toList());

			if (!chosenFunction.constant && ether.isConnected()) {
				BigDecimal valueWei = BigDecimal.valueOf(ether.getValue()).multiply(BigDecimal.TEN.pow(18));
				args.put("value", valueWei.toBigInteger().toString());
			}

			request = new HttpPost(ethereumOptions.getServer() + "/call");

		} else {
			// fallback function selected: send ether
			if (!chosenFunction.constant && ether.isConnected()) {
				BigDecimal valueWei = BigDecimal.valueOf(ether.getValue()).multiply(BigDecimal.TEN.pow(18));
				args.put("value", valueWei.toBigInteger().toString());
			} else {
				args.put("value", "0");
			}
			request = new HttpPost(ethereumOptions.getServer() + "/send");
		}

		String jsonString = gson.toJson(args);
		try {
			log.info("Sending function call: " + jsonString);
			request.setEntity(new StringEntity(jsonString));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return request;
	}

	public static class TransactionResponse {
		public Map<String, List<JsonElement>> events;		// event name -> args in order they're in ABI
		public Double valueSent;
		public Double valueReceived;
		public Double gasUsed;
		public Double gasPrice;
		public Double blockNumber;
		public Double nonce;
		public String txHash;
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
			if (response.get("errors") != null) { throw new RuntimeException(response.get("errors").toString()); }

			if (chosenFunction.constant) {
				JsonArray resultValues = response.get("results").getAsJsonArray();
				int n = Math.min(results.size(), resultValues.size());
				for (int i = 0; i < n; i++) {
					String result = resultValues.get(i).getAsString();
					Output<Object> output = results.get(i);
					convertAndSend(output, result);
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
				txHash.send(resp.txHash);
				for (EthereumABI.Event ev : abi.getEvents()) {
					List<JsonElement> args = resp.events.get(ev.name);
					List<Output<Object>> evOutputs = eventOutputs.get(ev);
					if (args != null) {
						if (ev.inputs.size() > 0) {
							int n = Math.min(evOutputs.size(), args.size());
							for (int i = 0; i < n; i++) {
								String value = args.get(i).getAsString();
								Output output = evOutputs.get(i);
								convertAndSend(output, value);
							}
						} else {
							evOutputs.get(0).send(Boolean.TRUE);
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

	public static void convertAndSend(Output output, String value) {
		if (output instanceof StringOutput) {
			output.send(value);
		} else if (output instanceof BooleanOutput) {
			output.send(Boolean.parseBoolean(value));
		} else if (output instanceof TimeSeriesOutput) {
			output.send(Double.parseDouble(value));
		} else {
			output.send(value);
		}
	}

	public static class FunctionNameParameter extends StringParameter {
		public List<EthereumABI.Function> list;

		public FunctionNameParameter(AbstractSignalPathModule owner, String name) {
			super(owner, name, "");
		}

		@Override
		protected List<PossibleValue> getPossibleValues() {
			List<PossibleValue> ret = new ArrayList<>();
			for (EthereumABI.Function f : list) {
				if (f.name.length() < 1) {
					ret.add(new PossibleValue("(default)", ""));	// fallback function
				} else {
					ret.add(new PossibleValue(f.name, f.name));
				}
			}
			return ret;
		}

		public EthereumABI.Function getSelected() {
			String v = this.getValue();
			for (EthereumABI.Function f : list) {
				if (f.name.equals(v)) {
					return f;
				}
			}
			return null;
		}
	}
}
