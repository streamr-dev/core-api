package com.unifina.signalpath.blockchain;

import com.streamr.client.protocol.message_layer.ITimestamped;
import com.unifina.datasource.IStartListener;
import com.unifina.datasource.IStopListener;
import com.unifina.signalpath.*;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.web3j.abi.EventValues;
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
public class GetEvents extends AbstractSignalPathModule implements EventsListener, IStartListener, IStopListener {
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
	private static final int CHECK_RESULT_MAX_TRIES = 100;
	private static final int CHECK_RESULT_WAIT_MS = 10000;

	static class LogsResult implements ITimestamped {
		Date timestamp;
		List<? extends Log> logs;
		String txHash;

		public LogsResult(String txHash, Date timestamp, List<? extends Log> logs) {
			this.timestamp = timestamp;
			this.logs = logs;
			this.txHash = txHash;
		}

		@Override
		public Date getTimestampAsDate() {
			return timestamp;
		}
	}

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

			getGlobals().getDataSource().addStartListener(this);
			getGlobals().getDataSource().addStopListener(this);
		}
	}

	@Override
	public void onStart() {
		String rpcUrl = ethereumOptions.getWebsocketRpcUri();
		if (rpcUrl == null) {
			rpcUrl = ethereumOptions.getRpcUrl();
			log.warn("No websockets URI found in config for network " + ethereumOptions.getNetwork() + ". Trying to run GetEvents over https RPC: " + rpcUrl);
		}
		String contractAddress = contract.getValue().getAddress();
		try {
			contractEventPoller = new ContractEventPoller(rpcUrl, contractAddress, this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		new Thread(contractEventPoller, "ContractEventPoller-Thread").start();
	}

	@Override
	public void onStop() {
		contractEventPoller.close();
	}

	private Propagator getPropagator() {
		if (asyncPropagator == null) {
			asyncPropagator = new Propagator(this);
		}
		return asyncPropagator;
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

	private void enqueueEvent(LogsResult lr) {
		getGlobals().getDataSource().enqueue(
			new com.unifina.data.Event<>(lr, lr.getTimestampAsDate(), event -> {
				try {
					displayEventsFromLogs(event);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				getPropagator().propagate();
			}));
	}

	@Override
	public void onEvent(JSONArray events) {
		int eventcount = events.length();
		for (int i = 0; i < eventcount; i++) {
			try {
				String txHash = events.getJSONObject(i).getString("transactionHash");
				log.info(String.format("Received event from RPC '%s'", txHash));
				TransactionReceipt txr = Web3jHelper.waitForTransactionReceipt(web3j, txHash, CHECK_RESULT_WAIT_MS, CHECK_RESULT_MAX_TRIES);
				if (txr == null) {
					log.error("Couldnt find TransactionReceipt for transaction " + txHash + " in allotted time");
					return;
				}
				long blockts = -1;
				try {
					blockts = Web3jHelper.getBlockTime(web3j, txr);
				} catch (Web3jHelper.BlockchainException e) {
					// log but don't (re)throw if getBlockTime fails. This happens often. Just use current time.
					// TODO maybe wait until block is present in RPC
					log.error(e.getMessage());
				}

				Date ts;
				if (blockts < 0) {
					ts = getGlobals().time;
					log.error("No block timestamp found for txHash " + txHash + ". Using globals timestamp " + ts);
				} else {
					ts = new Date(blockts * 1000);
				}
				enqueueEvent(new LogsResult(txHash, ts, txr.getLogs()));
			} catch (JSONException | IOException e) {
				onError(e.getMessage());
			}

		}
	}

	@Override
	public void onError(String message) {
		errors.send(Collections.singletonList(message));
		getPropagator().propagate();
	}

	protected void displayEventsFromLogs(LogsResult lr) {
		log.info("Received LogsResult for tx " + lr.txHash);

		txHashOutput.send(lr.txHash);
		for (EthereumABI.Event abiEvent : contract.getValue().getABI().getEvents()) {
			List<Output> eventOutputs = outputsByEvent.get(abiEvent.name);
			Event web3jEvent = web3jEvents.get(abiEvent.name);
			List<EventValues> valueList = Web3jHelper.extractEventParameters(web3jEvent, lr.logs);
			if (valueList == null) {
				throw new RuntimeException("Failed to get event params");
			}
			//valueList contains only the events of type abiEvent
			for (EventValues ev : valueList) {
				if (abiEvent.inputs.size() > 0) {
					// indexed and non-indexed event args are saved differently in logs and must be retrieved by different methods
					// see https://solidity.readthedocs.io/en/v0.5.3/contracts.html#events
					int nextIndexed = 0, nextNonIndexed = 0;
					for (int i = 0; i < abiEvent.inputs.size(); i++) {
						EthereumABI.Slot s = abiEvent.inputs.get(i);
						Output output = eventOutputs.get(i);
						Object value;
						if (s.indexed) {
							value = ev.getIndexedValues().get(nextIndexed++).getValue();
						} else {
							value = ev.getNonIndexedValues().get(nextNonIndexed++).getValue();
						}
						convertAndSend(output, value);
					}
				} else {
					// events with no arguments just get a true sent as a sign of event being triggered
					eventOutputs.get(0).send(Boolean.TRUE);
				}
			}
		}
	}


	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions = EthereumModuleOptions.readFrom(options);
		if (contract.hasValue()) {
			String network = contract.getValue().getNetwork();
			if (network != null) {
				log.info("Setting ethereumOptions.network = " + network + ", passed from Contract");
				ethereumOptions.setNetwork(network);
			}
		}
		web3j = getWeb3j();
		outputsByEvent = new HashMap<>();
		web3jEvents = new HashMap<>();
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

	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		ModuleOptions options = ModuleOptions.get(config);
		return config;
	}

}
