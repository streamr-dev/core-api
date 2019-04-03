package com.unifina.signalpath.blockchain;

import com.google.gson.*;
import com.unifina.data.FeedEvent;
import com.streamr.client.protocol.message_layer.ITimestamped;
import com.unifina.signalpath.*;
import com.unifina.signalpath.remote.AbstractHttpModule;
import com.unifina.utils.MapTraversal;
import jdk.nashorn.internal.ir.RuntimeNode;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.AbiTypes;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Send out a call to specified function in Ethereum block chain
 */
public class SendEthereumTransaction extends ModuleWithSideEffects {

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
	private Web3j web3j;
//	private Web3Bridge web3Bridge;
	@Override
	public void init() {
//		web3Bridge = new Web3Bridge();
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
		web3j = Web3j.build(new HttpService(ethereumOptions.getRpcUrl()));
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

	static class TransactionResult implements ITimestamped {
		String hash;
		Map<String, List<Object>> events;
		List<Object> results;
		Date timestamp;

		public TransactionResult(Date timestamp) {
			this.timestamp = timestamp;
		}

		@Override
		public Date getTimestampAsDate() {
			return timestamp;
		}
	}

	/**
	 * Asynchronously handle server response, call comes from event queue
	 * @param event containing HttpTransaction created within sendOutput
	 */
	@Override
	public void receive(FeedEvent event) {
		if (event.content instanceof TransactionResult) {
			sendOutput((TransactionResult)event.content);
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

	/**
	 * translate string type name to web3j Type Class
	 * @param type
	 * @return
	 */
	public static Class getTypeClass(String type) {
		if(type.endsWith("[]")){
			return Array.class;
		}
		switch(type){
			case "int":return Int.class;
			case "uint":return Uint.class;
		}
		Class c =AbiTypes.getType(type);
		return c;
	}

	protected static BigInteger asBigInteger(Object arg){
		if(arg instanceof BigInteger)
			return (BigInteger) arg;
		else if(arg instanceof BigDecimal) {
			return ((BigDecimal) arg).toBigInteger();
		}
		else if(arg instanceof String) {
			return Numeric.toBigInt((String) arg);
		}
		else if(arg instanceof Number)
			return BigInteger.valueOf(((Number) arg).longValue());
		return null;
	}

	protected static Type instantiateType(Class type, Object arg) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		if(type == null || !Type.class.isAssignableFrom(type))
			throw new ClassCastException("Class "+type+" must be a subclass of web3j Type");

		if(Array.class.isAssignableFrom(type)) {
			 if(!(arg instanceof List))
				 throw new ClassCastException("Arg of type "+arg.getClass()+" should be a list to instantiate web3j Array Type");
			List arglist = (List) arg;
			Constructor listcons = type.getConstructor(new Class[]{List.class});
			//instantiate a sample of the array to see what class it holds
			Array sample = (Array) listcons.newInstance(Collections.EMPTY_LIST);
			Class valueclass = sample.getClass();
			//create a list of transformed arguments
			ArrayList transformedList = new ArrayList(arglist.size());
			for(Object o : arglist){
				transformedList.add(instantiateType(valueclass,o));
			}
			return (Type) listcons.newInstance(transformedList);
		}


		Object constructorArg = null;
		if(NumericType.class.isAssignableFrom(type)){
			constructorArg = asBigInteger(arg);
		}
		else if(BytesType.class.isAssignableFrom(type)) {
			if(arg instanceof byte[])
				constructorArg = arg;
			else if(arg instanceof BigInteger) {
				constructorArg = ((BigInteger) arg).toByteArray();
			}
			else if(arg instanceof String){
				constructorArg = Numeric.hexStringToByteArray((String) arg);
			}
		}
		else if(Utf8String.class.isAssignableFrom(type)) {
			constructorArg = arg.toString();
		}
		else if(Address.class.isAssignableFrom(type)) {
			constructorArg = arg.toString();
		}
		else if(Bool.class.isAssignableFrom(type)) {
			if(arg instanceof Boolean){
				constructorArg = arg;
			}
			else {
				BigInteger bival = asBigInteger(arg);
				constructorArg = bival == null ? null : !bival.equals(BigInteger.ZERO);
			}
		}
		if(constructorArg == null){
			throw new RuntimeException("Could not create type "+type+" from arg "+arg.toString()+" of type "+arg.getClass());
		}
		Constructor cons = type.getConstructor(new Class[]{constructorArg.getClass()});
		return (Type) cons.newInstance(constructorArg);
	}

	protected Function createWeb3jFunctionCall() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		List<Type> inputs = new ArrayList<Type>(chosenFunction.inputs.size());
		for(int i=0;i<chosenFunction.inputs.size();i++){
			EthereumABI.Slot slot = chosenFunction.inputs.get(i);
			Input<Object> input = arguments.get(i);
//			Type t = TypeDecoder.decode(input.getValue().toString(),0, TypeReference.create(getTypeClass(slot.type)));
			Type t = instantiateType(getTypeClass(slot.type), input.getValue());
			inputs.add(t);
		}
		List<TypeReference<?>> outputs = new ArrayList<TypeReference<?>>(chosenFunction.outputs.size());
		for(int i=0;i<chosenFunction.outputs.size();i++){
			EthereumABI.Slot slot = chosenFunction.outputs.get(i);
			outputs.add(TypeReference.create(getTypeClass(slot.type)));
		}
		Function webjfn = new Function(chosenFunction.name,inputs,outputs);
		return webjfn;
	}



	protected BigInteger getGasLimit(){
		return BigInteger.valueOf(6000000l);
	}

	@Override
	protected void activateWithSideEffects() {
		// ethereumAccount
		// ethereumOptions
		EthereumContract c = contract.getValue();
		if (c == null || c.getABI() == null || function.list == null) { throw new RuntimeException("Faulty contract"); }
		if (chosenFunction == null) { throw new RuntimeException("Need valid function to call"); }
		if (c.getAddress() == null) { throw new RuntimeException("Contract must be deployed before calling"); }

		try {
			Function fn = createWeb3jFunctionCall();
			Credentials credentials = Credentials.create(ethereumAccount.getPrivateKey());
			String encodeFnCall = FunctionEncoder.encode(fn);
			//String encodeFnCall = web3Bridge.encodeMethodCall(abi,chosenFunction,arguments);
			EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
					ethereumAccount.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
			BigInteger nonce = ethGetTransactionCount.getTransactionCount();
			BigInteger gasPrice = BigDecimal.valueOf(ethereumOptions.getGasPriceWei()).toBigInteger();
			BigInteger valueWei;
			if (!chosenFunction.constant && ether.isConnected())
				valueWei = BigDecimal.valueOf(ether.getValue()).multiply(BigDecimal.TEN.pow(18)).toBigInteger();
			else
				valueWei = BigInteger.ZERO;

			//createTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, BigInteger value, String data) {
			RawTransaction rawTransaction = RawTransaction.createTransaction(nonce,gasPrice,getGasLimit(),c.getAddress(),valueWei,encodeFnCall);
			byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
			String hexValue = Numeric.toHexString(signedMessage);
			EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
			txHash.send(ethSendTransaction.getTransactionHash());
		}
		catch (Exception e){
			log.error(e.getMessage());
			throw new RuntimeException(e);
		}
//		Transaction.createContractTransaction()

		/*
		        return new Function(
                "totalSupply",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {}));
		 */

		//web3j.send().then(() -> {
			// TODO: populate
			TransactionResult tr = new TransactionResult(getGlobals().time);
			// push the response into Streamr's event queue
			getGlobals().getDataSource().enqueueEvent(new FeedEvent<>(tr, tr.timestamp, this));
		//});
	}

	public void sendOutput(TransactionResult tx) {
		if (chosenFunction.constant) {
			int n = Math.min(results.size(), tx.results.size());
			for (int i = 0; i < n; i++) {
				Object result = tx.results.get(i);
				Output<Object> output = results.get(i);
				convertAndSend(output, result);
			}
		} else {
			if (gson == null) { gson = new Gson(); }
			txHash.send(tx.hash);
			for (EthereumABI.Event ev : abi.getEvents()) {
				List<Object> args = tx.events.get(ev.name);
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
		// TODO (get from another branch)
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
	protected String getDummyNotificationMessage() {
		String recipient = contract.getValue().getAddress();
		if (chosenFunction.name.length() > 0) {
			return chosenFunction.name + " called on " + recipient;
		} else {
			return ether.getValue() + " ETH sent to " + recipient;
		}
    }

	/** Streamr-web3 ethereum bridge can be running on a local machine */
	protected boolean localAddressesAreAllowed() {
		return true;
	}
}
