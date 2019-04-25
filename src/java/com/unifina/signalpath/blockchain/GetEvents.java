package com.unifina.signalpath.blockchain;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.unifina.datasource.IStartListener;
import com.unifina.datasource.IStopListener;
import com.unifina.signalpath.*;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.IOException;
import java.util.*;

/**
 * Get events sent out by given contract in the given transaction
 */
public class GetEvents extends AbstractSignalPathModule implements ContractEventPoller.Listener, IStartListener, IStopListener {
	private static final Logger log = Logger.getLogger(GetEvents.class);

	private final EthereumContractInput contract = new EthereumContractInput(this, "contract");
	private final ListOutput errors = new ListOutput(this, "errors");

	private Map<String, List<Output>> outputsByEvent; // event name -> [output for each event argument]
	private Map<String, Event> web3jEvents; // event name -> Web3j.Event object

	private EthereumModuleOptions ethereumOptions = new EthereumModuleOptions();

	private transient ContractEventPoller contractEventPoller;
	private transient Propagator asyncPropagator;

	@Override
	public void init() {
		setPropagationSink(true);
		addInput(contract);
		addOutput(errors);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (getGlobals().isRunContext()) {
			if (!contract.hasValue()) {
				throw new RuntimeException("Contract input must have a value.");
			}

			asyncPropagator = new Propagator(this);
			getGlobals().getDataSource().addStartListener(this);
			getGlobals().getDataSource().addStopListener(this);
		}
	}

	@Override
	public void onStart() {
		String rpcUrl = ethereumOptions.getRpcUrl();
		String contractAddress = contract.getValue().getAddress();
		contractEventPoller = new ContractEventPoller(rpcUrl, contractAddress, this);
		new Thread(contractEventPoller, "ContractEventPoller-Thread").start();
	}

	@Override
	public void onStop() {
		contractEventPoller.close();
	}


	@Override
	public void sendOutput() {}

	@Override
	public void clearState() {}

	@Override
	public void onEvent(JSONArray events) {
		try {
			String txHash = events.getJSONObject(0).getString("transactionHash");
			log.info(String.format("Received event '%s'", txHash));

			// TODO: web3j
			TransactionReceipt txr = Web3jHelper.getTransactionReceipt(null, txHash);

			for (EthereumABI.Event abiEvent : contract.getValue().getABI().getEvents()) {
				List<Output> eventOutputs = outputsByEvent.get(abiEvent.name);
				Event web3jEvent = web3jEvents.get(abiEvent.name);
				List<EventValues> valueList = Web3jHelper.extractEventParameters(web3jEvent, txr);
				if (valueList == null) {
					throw new RuntimeException("Failed to get event params");	// TODO: what happens when you throw in poller thread?
				}

				if (abiEvent.inputs.size() > 0) {
					// TODO: parse events from EventValues
					int n = Math.min(eventOutputs.size(), valueList.size());
					for (int i = 0; i < n; i++) {
						String value = valueList.get(i).getIndexedValues().get(i).toString();
						Output output = eventOutputs.get(i);
						EthereumCall.convertAndSend(output, value);
					}
				} else {
					// events with no arguments just get a true sent as a sign of event being triggered
					eventOutputs.get(0).send(Boolean.TRUE);
				}
			}
			asyncPropagator.propagate();

		} catch (JSONException | IOException e) {
			onError(e.getMessage());
		}
	}

	@Override
	public void onError(String message) {
		errors.send(Collections.singletonList(message));
		asyncPropagator.propagate();
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		outputsByEvent = new HashMap<>();

		if (contract.hasValue()) {
			EthereumABI abi = contract.getValue().getABI();
			for (EthereumABI.Event abiEvent : abi.getEvents()) {
				// create outputs
				List<Output> eventOutputs = new ArrayList<>();
				if (abiEvent.inputs.size() > 0) {
					for (EthereumABI.Slot arg : abiEvent.inputs) {
						String displayName = abiEvent.name + "." + (arg.name.length() > 0 ? arg.name : "(" + arg.type + ")");
						Output output = EthereumToStreamrTypes.asOutput(arg.type, displayName, this);
						eventOutputs.add(output);
						addOutput(output);
					}
				} else {
					// event without parameters/arguments/inputs
					Output output = new BooleanOutput(this, abiEvent.name);
					eventOutputs.add(output);
					addOutput(output);
				}
				outputsByEvent.put(abiEvent.name, eventOutputs);

				// cache the web3j Event object
				ArrayList<TypeReference<?>> params = new ArrayList<TypeReference<?>>();
				for (EthereumABI.Slot s : abiEvent.inputs) {
					try {
						params.add(Web3jHelper.makeTypeReference(s.type));
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(e);
					}
				}
				Event web3jEvent = new Event(abiEvent.name, params);
				web3jEvents.put(abiEvent.name, web3jEvent);
			}
		}

		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions = EthereumModuleOptions.readFrom(options);
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions.writeNetworkOption(options);
		return config;
	}
}
