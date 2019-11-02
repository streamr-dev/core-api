package com.unifina.signalpath.blockchain;

import com.streamr.client.protocol.message_layer.ITimestamped;
import com.unifina.data.Event;
import com.unifina.datasource.IStartListener;
import com.unifina.datasource.IStopListener;
import com.unifina.signalpath.*;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.web3j.abi.EventValues;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Get events sent out by given contract in the given transaction
 */
public class GetEvents extends AbstractSignalPathModule implements EventsListener, IStartListener, IStopListener {
	private static final Logger log = Logger.getLogger(GetEvents.class);

	private final EthereumContractInput contract = new EthereumContractInput(this, "contract");
	private final ListOutput errors = new ListOutput(this, "errors");
	private StringOutput txHashOutput = new StringOutput(this, "txHash");

	private Map<String, List<Output>> outputsByEvent; // event name -> [output for each event argument]
	private Map<String, org.web3j.abi.datatypes.Event> web3jEvents;

	private EthereumOptions ethereumOptions = new EthereumOptions();

	private transient ContractEventPoller contractEventPoller;
	private static final int CHECK_RESULT_MAX_TRIES = 100;
	private static final int CHECK_RESULT_WAIT_MS = 10000;

	@Override
	public void init() {
		setPropagationSink(true);
		addInput(contract);
		addOutput(errors);
		addOutput(txHashOutput);
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
			log.warn("No websockets URI found in config for network " + ethereumOptions.network + ". Trying to run GetEvents over https RPC: " + rpcUrl);
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
		if (contractEventPoller != null) {
			contractEventPoller.close();
		}
	}

	@Override
	public void sendOutput() {
	}

	@Override
	public void clearState() {
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		if (contract.hasValue()) {
			ethereumOptions = contract.getValue().getEthereumOptions();
		}
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
					web3jEvents.put(abiEvent.name, Web3jHelper.toWeb3jEvent(abiEvent));
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	public void onEvent(JSONArray events) {
		int eventcount = events.length();
		for (int i = 0; i < eventcount; i++) {
			Date timestamp = getGlobals().time;

			String txHash = events.getJSONObject(i).getString("transactionHash");
			TransactionReceipt txr;
			try {
				log.info(String.format("Received event from RPC '%s'", txHash));
				txr = Web3jHelper.waitForTransactionReceipt(getWeb3j(), txHash, CHECK_RESULT_WAIT_MS, CHECK_RESULT_MAX_TRIES);
				try {
					long blockts = Web3jHelper.getBlockTime(getWeb3j(), txr);
					timestamp = new Date(blockts * 1000);
				} catch (Web3jHelper.BlockchainException e) {
					// log but don't (re)throw if getBlockTime fails. This happens often. Just use current time.
					// TODO maybe wait until block is present in RPC
					log.error("Failed to get timestamp for txHash " + txHash + ". Using globals timestamp " + timestamp, e);
				}
			} catch (IOException | TimeoutException e) {
				log.error("Failed to get transaction receipt", e);
				onError(e.getMessage());
				continue;
			}

			txHashOutput.send(txHash);
			for (EthereumABI.Event abiEvent : contract.getValue().getABI().getEvents()) {
				org.web3j.abi.datatypes.Event web3jEvent = web3jEvents.get(abiEvent.name);
				List<Log> logs = txr.getLogs();
				List<EventValues> valueList = Web3jHelper.extractEventParameters(web3jEvent, logs);

				// valueList contains only the events of type abiEvent
				for (EventValues ev : valueList) {
					List<Object> values = new ArrayList<>();
					if (abiEvent.inputs.size() > 0) {
						// indexed and non-indexed event args are saved differently in logs and must be retrieved by different methods
						// see https://solidity.readthedocs.io/en/v0.5.3/contracts.html#events
						int nextIndexed = 0, nextNonIndexed = 0;
						for (int j = 0; j < abiEvent.inputs.size(); j++) {
							EthereumABI.Slot s = abiEvent.inputs.get(j);
							Object value;
							if (s.indexed) {
								value = ev.getIndexedValues().get(nextIndexed++).getValue();
							} else {
								value = ev.getNonIndexedValues().get(nextNonIndexed++).getValue();
							}
							values.add(value);
						}
					} else {
						// events with no arguments just get a true sent as a sign of event being triggered
						values.add(Boolean.TRUE);
					}

					// TODO: if only one event, send directly?
					enqueueSend(abiEvent.name, values, timestamp, i);
				}
			}
		}
	}

	private transient Web3j web3j;
	protected Web3j getWeb3j() {
		if (web3j == null && ethereumOptions != null) {
			web3j = ethereumOptions.getWeb3j();
		}
		return web3j;
	}

	@Override
	public void onError(String message) {
		errors.send(Collections.singletonList(message));
		getPropagator().propagate();
	}

	public void enqueueSend(String eventName, List<Object> values, Date timestamp, long sequenceNumber) {
		// TODO: is something lost by pulling in the whole context of GetEvents module instance?
		// TODO: alternative would be to wrap the (outputs, values) into a QueuedSend object
		//QueuedSend queuedSend = new QueuedSend(eventName, values, timestamp);
		//Event<QueuedSend> event = new Event<>(queuedSend, timestamp, sequenceNumber, send -> {
		Event<Object> event = new Event<>(null, timestamp, sequenceNumber, dummyArg -> {
			List<Output> outputs = outputsByEvent.get(eventName);
			int n = values.size();
			assert outputs.size() == n : "Output count for " + eventName + " = " + outputs.size() + ", expected " + n;
			for (int i = 0; i < n; i++) {
				Output output = outputs.get(i);
				Object value = values.get(i);
				EthereumToStreamrTypes.convertAndSend(output, value);
			}
			getPropagator().propagate();
		});
		getGlobals().getDataSource().enqueue(event);
	}

	private transient Propagator queuedSendPropagator;
	private Propagator getPropagator() {
		if (queuedSendPropagator == null) {
			queuedSendPropagator = new Propagator(this);
		}
		return queuedSendPropagator;
	}

//	public static class QueuedSend implements ITimestamped {
//		public String eventName;
//		public List<Object> values;
//		public Date timestamp;
//
//		public QueuedSend(String eventName, List<Object> values, Date timestamp) {
//			this.eventName = eventName;
//			this.values = values;
//			this.timestamp = timestamp;
//		}
//
//		@Override
//		public Date getTimestampAsDate() {
//			return timestamp;
//		}
//	}
}
