package com.unifina.signalpath.blockchain;

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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Send out a call to specified function in Ethereum block chain
 */
public class SendEthereumTransaction extends AbstractSignalPathModule {

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
	private StringOutput txHash = new StringOutput(this, "txHash");
	private Map<EthereumABI.Event, List<Output<Object>>> eventOutputs;		// outputs for each event separately

	private static final Logger log = Logger.getLogger(SendEthereumTransaction.class);
	private static final int ETHEREUM_TRANSACTION_DEFAULT_TIMEOUT_SECONDS = 1800;

	private transient Gson gson;

	@Override
	public void init() {
		addInput(ethereumAccount);
		addInput(contract);
		contract.setDrivingInput(false);
		contract.setCanToggleDrivingInput(false);

		function.setUpdateOnChange(true);		// update argument inputs

		trigger.setCanToggleDrivingInput(false);
		trigger.setDrivingInput(true);
		trigger.setRequiresConnection(false);
		ether.setRequiresConnection(false);

		addOutput(errors);
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

		// When running, try accessing the private key - fail fast if access is denied
		if (getGlobals().isRunContext()) {
			ethereumAccount.getPrivateKey();
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
			addOutput(txHash);
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

	public void sendOutput() {
		// ethereumAccount
		// ethereumOptions
		EthereumContract c = contract.getValue();
		// chosenFunction
		// arguments
		double ethToSend = ether.getValue();	// whole eth, use toWei to convert

		// TODO: send ethereum transaction, populate following variables:
		String txHashStr = "";
		List<Object> events;

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
			txHash.send(txHashStr);
			for (EthereumABI.Event ev : abi.getEvents()) {
				List<JsonElement> args = events.get(ev.name);
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
	}

	static void convertAndSend(Output output, String value) {
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

	/** Message to show in UI in historical/side-effect-free mode */
	@Override
	protected String getDummyNotificationMessage() {
		String recipient = contract.getValue().getAddress();
		if (chosenFunction.name.length() > 0) {
			return chosenFunction.name + " called on " + recipient;
		} else {
			return ether.getValue() + " ETH sent to " + recipient;
		}
    }

	/** Streamr-web3 ethereum bridge can be running on a local machine */
	@Override
	protected boolean localAddressesAreAllowed() {
		return true;
	}
}
