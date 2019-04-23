package com.unifina.signalpath.blockchain;

import com.google.gson.Gson;
import com.unifina.data.FeedEvent;
import com.streamr.client.protocol.message_layer.ITimestamped;
import com.unifina.signalpath.*;
import com.unifina.utils.MapTraversal;
import org.apache.log4j.Logger;
import org.web3j.abi.*;
import org.web3j.abi.datatypes.*;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Send out a call to specified function in Ethereum block chain
 */
public class SendEthereumTransaction extends ModuleWithSideEffects {

	private EthereumModuleOptions ethereumOptions = new EthereumModuleOptions();

	private final EthereumAccountParameter ethereumAccount = new EthereumAccountParameter(this, "ethAccount");
	private EthereumContractInput contract = new EthereumContractInput(this, "contract");

	private ListOutput errors = new ListOutput(this, "errors");
	private TimeSeriesInput ether = new TimeSeriesInput(this, "ether");
	private Input<Object> trigger = new Input<>(this, "trigger", "Object");        // shown if there are no inputs

	private EthereumABI abi;
	private EthereumABI.Function chosenFunction;

	private FunctionNameParameter function = new FunctionNameParameter(this, "function");
	private List<Input<Object>> arguments = new ArrayList<>();

	protected int check_result_max_tries = 100;
	protected int check_result_waitms = 10000;

	// constant function outputs
	private List<Output<Object>> results = new ArrayList<>();

	// transaction outputs
	private StringOutput txHash = new StringOutput(this, "txHash");
	private Map<EthereumABI.Event, List<Output<Object>>> eventOutputs;        // outputs for each event separately

	private TimeSeriesOutput valueSent = new TimeSeriesOutput(this, "spentEth");
	//private TimeSeriesOutput valueReceived = new TimeSeriesOutput(this, "targetChangeWei");
	private TimeSeriesOutput gasUsed = new TimeSeriesOutput(this, "gasUsed");
	private TimeSeriesOutput gasPrice = new TimeSeriesOutput(this, "gasPriceWei");
	private TimeSeriesOutput blockNumber = new TimeSeriesOutput(this, "blockNumber");
	private TimeSeriesOutput nonce = new TimeSeriesOutput(this, "nonce");


	private static final Logger log = Logger.getLogger(SendEthereumTransaction.class);
	private static final int ETHEREUM_TRANSACTION_DEFAULT_TIMEOUT_SECONDS = 1800;

	private Web3j web3j;

	@Override
	public void init() {
		addInput(ethereumAccount);
		addInput(contract);
		contract.setDrivingInput(false);
		contract.setCanToggleDrivingInput(false);

		function.setUpdateOnChange(true);        // update argument inputs

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

	protected Web3j getWeb3j(){
		return Web3j.build(new HttpService(ethereumOptions.getRpcUrl()));
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		//String stringconfig = new Gson().toJson(config);
		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions = EthereumModuleOptions.readFrom(options);
		web3j = getWeb3j();
		updateInterface();

		// Find the function input config and configure it manually.
		// The input is not yet configured, so otherwise it won't hold the correct value yet.
		if (config.containsKey("params")) {
			for (Object configObject : MapTraversal.getList(config, "params")) {
				Map<String, Object> inputConfig = (Map<String, Object>) configObject;
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
			int eventNum = 0;
			for (EthereumABI.Event ev : abi.getEvents()) {
				List<Output<Object>> evOutputs = new ArrayList<>();
				if (ev.inputs.size() > 0) {
					int argnum = 0;
					for (EthereumABI.Slot arg : ev.inputs) {
						String displayName = ev.name + "." + (arg.name.length() > 0 ? arg.name : "(" + arg.type + eventNum + "_" + argnum + ")");
						argnum++;
						Output output = EthereumToStreamrTypes.asOutput(arg.type, displayName, this);
						evOutputs.add(output);
					}
				} else {
					// event without parameters/arguments/inputs
					Output output = new BooleanOutput(this, ev.name);
					evOutputs.add(output);
				}
				eventOutputs.put(ev, evOutputs);
				eventNum++;
			}
		}
	}

	private void updateFunction() {
		if (function.list == null || function.list.size() < 1) {
			return;
		}

		addInput(function);
		chosenFunction = function.getSelected();
		if (chosenFunction == null) {
			log.warn("Can't find function " + function.getValue());
			chosenFunction = function.list.get(0);
			function.receive(chosenFunction.name);
		}
		log.info("Chose function " + chosenFunction.name);

		arguments.clear();
		int i = 0;
		for (EthereumABI.Slot s : chosenFunction.inputs) {
			String name = s.name;
			if (name.length() < 1) {
				name = "(" + s.type + ")_" + i;
			}
			i++;
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
			i = 0;
			for (EthereumABI.Slot s : chosenFunction.outputs) {
				String name = s.name;
				if (name.length() < 1) {
					name = "(" + s.type + ")_" + i;
				}
				i++;
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

			addOutput(valueSent);
//			addOutput(valueReceived);
			addOutput(gasUsed);
			addOutput(gasPrice);
			addOutput(blockNumber);
			addOutput(nonce);
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
	 * wraps Web3j Response with utility methods
	 */
	abstract class FunctionCallResult implements ITimestamped {
		Date timestamp;
		Function fn;

		public FunctionCallResult(Date timestamp, Function _fn) {
			this.timestamp = timestamp;
			fn = _fn;
		}

		@Override
		public Date getTimestampAsDate() {
			return timestamp;
		}

		public abstract Response getResponse();

		public Response.Error getError() {
			return getResponse().getError();
		}
	}

	class ConstantFunctionResult extends FunctionCallResult {
		private EthCall ethcall;

		public ConstantFunctionResult(Date timestamp, Function _fn, EthCall ec) {
			super(timestamp, _fn);
			ethcall = ec;
		}

		@Override
		public Response getResponse() {
			return ethcall;
		}

		public List<Type> getResults() {
			return FunctionReturnDecoder.decode(
				ethcall.getValue(), fn.getOutputParameters());
		}
	}

	/**
	 * non constant function call result
	 */
	class TransactionResult extends FunctionCallResult {
		private EthSendTransaction web3jTx;
		private TransactionReceipt receipt;
		private org.web3j.protocol.core.methods.response.Transaction transaction;

		public TransactionResult(Date timestamp, Function _fn, EthSendTransaction tx) {
			super(timestamp, _fn);
			web3jTx = tx;
		}

		/**
		 * fetch tx data and populate transaction and receipt when done.
		 * run this from a worker thread (eg async submit tx handler)
		 */
		protected void enqueueConfirmedTx() throws IOException {
			final FunctionCallResult fncall = this;
				int tries = 0;
				while (tries++ < check_result_max_tries) {
					EthGetTransactionReceipt get = web3j.ethGetTransactionReceipt(web3jTx.getTransactionHash()).send();
					if ((receipt = get.getResult()) != null) {
						transaction = web3j.ethGetTransactionByHash(web3jTx.getTransactionHash()).send().getResult();
						log.info("Receipt found for txHash: " + web3jTx.getTransactionHash());
						enqueueEvent(fncall);
						return;
					}
					log.info("No receipt found for txHash: " + web3jTx.getTransactionHash());
					if(tries == check_result_max_tries)
						continue;
					try {
						log.info(". Waiting "+check_result_waitms+"ms");
						Thread.sleep(check_result_waitms);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
				log.error("Couldnt find receipt for txHash: " + web3jTx.getTransactionHash());
		}

		@Override
		public Response getResponse() {
			return web3jTx;
		}

		public String getTransactionHash() {
			return web3jTx.getTransactionHash();
		}

		public org.web3j.protocol.core.methods.response.Transaction getTransaction(){
			return transaction;
		}

		public TransactionReceipt getTransactionReceipt(){
			return receipt;
		}

		public Map<String, List<Object>> getEvents() throws IOException, ClassNotFoundException {
			Map<String, List<Object>> events = new HashMap<String, List<Object>>();
			TransactionReceipt txr = getTransactionReceipt();
			if (txr == null) {
				log.info("couldnt find transaction receipt for txhash: " + web3jTx.getTransactionHash());
				return null;
			}
			for (EthereumABI.Event e : abi.getEvents()) {
				ArrayList<TypeReference<?>> params = new ArrayList<TypeReference<?>>();
				for (EthereumABI.Slot s : e.inputs) {
					params.add(Web3jHelper.makeTypeReference(s.type));
				}
				Event web3jevent = new Event(e.name, params);
				List<EventValues> valueList = Web3jHelper.extractEventParameters(web3jevent, txr);
				if (valueList == null) {
					continue;
				}
				for (EventValues ev : valueList) {
					List<Object> objs = new ArrayList<Object>();
					List<Type> niv = ev.getNonIndexedValues();
					for (Type t : niv) {
						objs.add(t.getValue());
					}
					events.put(e.name, objs);
				}
			}
			return events;
		}

	}

	/**
	 * Asynchronously handle server response, call comes from event queue
	 *
	 * @param event containing HttpTransaction created within sendOutput
	 */
	@Override
	public void receive(FeedEvent event) {
		if (event.content instanceof FunctionCallResult) {
			try {
				sendOutput((FunctionCallResult) event.content);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			getPropagator().propagate();
		} else {
			super.receive(event);
		}
	}

	private transient Propagator asyncPropagator;

	private Propagator getPropagator() {
		if (asyncPropagator == null) {
			asyncPropagator = new Propagator(this);
		}
		return asyncPropagator;
	}

	// TODO: get old tx and send out correct values (TODO: create another JIRA ticket for that)
	@Override
	protected void activateWithoutSideEffects() {
		String msg = "Real-time action by '" + getEffectiveName() + "' ignored in historical mode:\n\n" + getDummyNotificationMessage();
		getParentSignalPath().showNotification(msg);
	}


	protected Function createWeb3jFunctionCall() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		List<String> solidity_inputtypes = new ArrayList<String>(chosenFunction.inputs.size());
		List args = new ArrayList(chosenFunction.inputs.size());
		List<String> solidity_outputtypes = new ArrayList<String>(chosenFunction.outputs.size());

		for (int i = 0; i < chosenFunction.inputs.size(); i++) {
			solidity_inputtypes.add(chosenFunction.inputs.get(i).type);
			args.add(arguments.get(i).getValue());
		}
		for (int i = 0; i < chosenFunction.outputs.size(); i++) {
			solidity_outputtypes.add(chosenFunction.outputs.get(i).type);
		}
		return Web3jHelper.encodeFunction(chosenFunction.name, solidity_inputtypes, args, solidity_outputtypes);
	}


	protected BigInteger getGasLimit() {
		return BigInteger.valueOf(6000000l);
	}

	@Override
	protected void activateWithSideEffects() {
		EthereumContract c = contract.getValue();
		if (c == null || c.getABI() == null || function.list == null) {
			throw new RuntimeException("Faulty contract");
		}
		if (chosenFunction == null) {
			throw new RuntimeException("Need valid function to call");
		}
		if (c.getAddress() == null) {
			throw new RuntimeException("Contract must be deployed before calling");
		}

		try {
			Function fn = createWeb3jFunctionCall();
			String encodeFnCall = FunctionEncoder.encode(fn);
			if (chosenFunction.constant) {
				EthCall response = web3j.ethCall(
					Transaction.createEthCallTransaction(ethereumAccount.getAddress(), c.getAddress(), encodeFnCall),
					DefaultBlockParameterName.LATEST).send();
				ConstantFunctionResult rslt = new ConstantFunctionResult(getGlobals().time, fn, response);
				enqueueEvent(rslt);
			} else {
				BigInteger gasPrice = BigDecimal.valueOf(ethereumOptions.getGasPriceWei()).toBigInteger();
				EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
					ethereumAccount.getAddress(), DefaultBlockParameterName.LATEST).send();
				BigInteger nonce = ethGetTransactionCount.getTransactionCount();
				BigInteger valueWei;
				Credentials credentials = Credentials.create(ethereumAccount.getPrivateKey());
				if (!chosenFunction.constant && ether.isConnected()) {
					valueWei = BigDecimal.valueOf(ether.getValue()).multiply(BigDecimal.TEN.pow(18)).toBigInteger();
				} else {
					valueWei = BigInteger.ZERO;
				}

				RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, getGasLimit(), c.getAddress(), valueWei, encodeFnCall);
				byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
				String hexValue = Numeric.toHexString(signedMessage);
				CompletableFuture<EthSendTransaction> cf = web3j.ethSendRawTransaction(hexValue).sendAsync();
				cf.thenAccept(new Consumer<EthSendTransaction>() {
					@Override
					public void accept(EthSendTransaction tx) {
						log.debug("TX response: " + tx.getRawResponse());
						TransactionResult tr = new TransactionResult(getGlobals().time, fn, tx);

						//enqueue the txHash only when submitted, before result from blockchain
						//enqueueEvent(tr);

						try {
							//if tx submit had error, don't wait to confirm tx
							if(tr.getError() != null)
								enqueueEvent(tr);
							else
							//enqueue the result from blockchain, when tx finishes
								tr.enqueueConfirmedTx();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				});
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// push the response into Streamr's event queue
	protected void enqueueEvent(FunctionCallResult fc){
		getGlobals().getDataSource().enqueueEvent(new FeedEvent<FunctionCallResult, SendEthereumTransaction>(fc, fc.timestamp, this));
	}

	/**
	 * format the list as an array of Strings
	 * @param l
	 * @return
	 */
	public static List stringifyTypeList(List l){
		ArrayList<String> printed = new ArrayList<String>(l.size());
		for(Object o : l){
			printed.add(typeToString(o));
		}
		return printed;
	}

	public static String typeToString(Object o){
		if(!(o instanceof List)) {
			if(o instanceof Type){
				return ((Type) o).getValue().toString();
			}
			else
				return o.toString();
		}
		StringBuilder sb = new StringBuilder();
		List l = (List) o;
		sb.append("[");
		int i=0;
		for(Object li : l){
			if(i++ > 0)
				sb.append(",");
			sb.append(typeToString(li));
		}
		sb.append("]");
		return sb.toString();
	}

	public void sendOutput(FunctionCallResult rslt) throws IOException, ClassNotFoundException {
		Response.Error e = rslt.getError();
		if (e != null) {
			errors.send(Arrays.asList(e.getMessage()));
		}
		if (rslt instanceof ConstantFunctionResult) {
			ConstantFunctionResult crslt = (ConstantFunctionResult) rslt;
			List<Type> txResults = crslt.getResults();
			if (txResults == null) {
				return;
			}
			int n = Math.min(results.size(), txResults.size());
			for (int i = 0; i < n; i++) {
				Object val = txResults.get(i).getValue();
				if(val instanceof List)
					val = stringifyTypeList((List) val);
				Output<Object> output = results.get(i);
				convertAndSend(output, val);
			}
		} else if (rslt instanceof TransactionResult) {
			TransactionResult tx = (TransactionResult) rslt;
			txHash.send(tx.getTransactionHash());

			TransactionReceipt txreceipt = tx.getTransactionReceipt();
			if(txreceipt != null){
				gasUsed.send(txreceipt.getGasUsed().longValue());
				blockNumber.send(txreceipt.getBlockNumber().longValue());
			}
			org.web3j.protocol.core.methods.response.Transaction txconfirm = tx.getTransaction();
			if(txconfirm != null){
				gasPrice.send(txconfirm.getGasPrice().longValue());
				valueSent.send(new BigDecimal(txconfirm.getValue()).divide(BigDecimal.TEN.pow(18)).doubleValue());
				nonce.send(txconfirm.getNonce().longValue());
			}

			Map<String, List<Object>> events = tx.getEvents();
			if(events != null)
				for (EthereumABI.Event ev : abi.getEvents()) {
					List<Object> args = events.get(ev.name);
					List<Output<Object>> evOutputs = eventOutputs.get(ev);
					if (args != null) {
						if (ev.inputs.size() > 0) {
						int n = Math.min(evOutputs.size(), args.size());
						for (int i = 0; i < n; i++) {
							Object value = args.get(i);
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
					ret.add(new PossibleValue("(default)", ""));    // fallback function
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

	/**
	 * Message to show in UI in historical/side-effect-free mode
	 */
	protected String getDummyNotificationMessage() {
		String recipient = contract.getValue().getAddress();
		if (chosenFunction.name.length() > 0) {
			return chosenFunction.name + " called on " + recipient;
		} else {
			return ether.getValue() + " ETH sent to " + recipient;
		}
	}

	/**
	 * Streamr-web3 ethereum bridge can be running on a local machine
	 */
	protected boolean localAddressesAreAllowed() {
		return true;
	}
}
