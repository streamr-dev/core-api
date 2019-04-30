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
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.util.*;

/**
 * Get events sent out by given contract in the given transaction
 */
public class GetEvents extends AbstractSignalPathModule implements ContractEventPoller.Listener, IStartListener, IStopListener {
	private static final Logger log = Logger.getLogger(GetEvents.class);

	private final EthereumContractInput contract = new EthereumContractInput(this, "contract");
	private final ListOutput errors = new ListOutput(this, "errors");
	private StringOutput txHashOutput = new StringOutput(this, "txHash");

	private Map<String, List<Output>> outputsByEvent; // event name -> [output for each event argument]
	private Map<String, Event> web3jEvents; // event name -> Web3j.Event object

	private EthereumModuleOptions ethereumOptions = new EthereumModuleOptions();

	private transient ContractEventPoller contractEventPoller;
	private transient Propagator asyncPropagator;
	private Web3j web3j;

	@Override
	public void init() {
		setPropagationSink(true);
		addInput(contract);
		addOutput(errors);
		addOutput(txHashOutput);

	}

	protected Web3j getWeb3j() {
		return Web3j.build(new HttpService(ethereumOptions.getRpcUrl()));
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
	public void sendOutput() {
	}

	@Override
	public void clearState() {
	}

	static void convertAndSend(Output output, Object value) {
		if (output instanceof StringOutput) {
			output.send(value);
		} else if (output instanceof BooleanOutput) {
			output.send(Boolean.parseBoolean(value.toString()));
		} else if (output instanceof TimeSeriesOutput) {
			output.send(Double.parseDouble(value.toString()));
		} else {
			output.send(value);
		}
	}

	@Override
	public void onEvent(JSONArray events) {
		try {
			String txHash = events.getJSONObject(0).getString("transactionHash");
			txHashOutput.send(txHash);
			log.info(String.format("Received event '%s'", txHash));
			TransactionReceipt txr = Web3jHelper.getTransactionReceipt(web3j, txHash);
			displayEventsFromLogs(txr.getLogs());
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

	protected void displayEventsFromLogs(List<? extends Log> logs){
		for (EthereumABI.Event abiEvent : contract.getValue().getABI().getEvents()) {
			List<Output> eventOutputs = outputsByEvent.get(abiEvent.name);
			Event web3jEvent = web3jEvents.get(abiEvent.name);
			List<EventValues> valueList = Web3jHelper.extractEventParameters(web3jEvent, logs);
			if (valueList == null) {
				throw new RuntimeException("Failed to get event params");	// TODO: what happens when you throw in poller thread?
			}

			if (abiEvent.inputs.size() > 0) {
				for (EventValues ev : valueList) {
					// indexed and non-indexed event args are saved differently in logs and must be retrieved by different methods
					// see https://solidity.readthedocs.io/en/v0.5.3/contracts.html#events
					int nextIndexed=0, nextNonIndexed=0;
					for(int i=0;i<abiEvent.inputs.size();i++){
						EthereumABI.Slot s = abiEvent.inputs.get(i);
						Output output = eventOutputs.get(i);
						Object value;
						if(s.indexed)
							value = ev.getIndexedValues().get(nextIndexed++).getValue();
						else
							value = ev.getNonIndexedValues().get(nextNonIndexed++).getValue();
						convertAndSend(output, value);
					}
				}
			} else {
				// events with no arguments just get a true sent as a sign of event being triggered
				eventOutputs.get(0).send(Boolean.TRUE);
			}
		}
	}


	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		web3j = getWeb3j();
		outputsByEvent = new HashMap<>();
		web3jEvents = new HashMap<String, Event>();
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
				try {
					Event web3jEvent = Web3jHelper.toWeb3jEvent(abiEvent);
					web3jEvents.put(abiEvent.name, web3jEvent);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
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
