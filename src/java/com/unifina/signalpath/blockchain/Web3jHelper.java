package com.unifina.signalpath.blockchain;

import org.apache.log4j.Logger;
import org.web3j.abi.*;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.AbiTypes;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Web3jHelper {
	private static final Logger log = Logger.getLogger(Web3jHelper.class);

	public static TransactionReceipt getTransactionReceipt(Web3j web3, String txhash) throws IOException {
		EthGetTransactionReceipt gtr = web3.ethGetTransactionReceipt(txhash).send();
		if (gtr == null) {
			log.error("TransactionHash not found: " + txhash);
			return null;
		}
		TransactionReceipt tr = gtr.getResult();
		if (tr == null) {
			log.error("TransactionReceipt not found for txhash: " + txhash);
			return null;
		}
		return tr;
	}
	public static Function toWeb3jFunction(EthereumABI.Function fn, List args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		List<String> solidity_inputtypes = new ArrayList<String>(fn.inputs.size());
		List<String> solidity_outputtypes = new ArrayList<String>(fn.outputs.size());

		for (int i = 0; i < fn.inputs.size(); i++) {
			solidity_inputtypes.add(fn.inputs.get(i).type);
		}
		for (int i = 0; i < fn.outputs.size(); i++) {
			solidity_outputtypes.add(fn.outputs.get(i).type);
		}
		return FunctionEncoder.makeFunction(fn.name, solidity_inputtypes, args, solidity_outputtypes);
	}

	public static Event toWeb3jEvent(EthereumABI.Event ev) throws ClassNotFoundException {
		ArrayList<TypeReference<?>> params = new ArrayList<TypeReference<?>>();
		for (EthereumABI.Slot s : ev.inputs) {
			params.add(TypeReference.makeTypeReference(s.type, s.indexed));
		}
		Event web3jevent = new Event(ev.name, params);
		return web3jevent;
	}

	public static List<EventValues> extractEventParameters(Event event, TransactionReceipt transactionReceipt) {
		List<Log> logs = transactionReceipt.getLogs();
		return extractEventParameters(event,logs);
	}
	public static List<EventValues> extractEventParameters(Event event, List<? extends Log> logs) {
		List<EventValues> values = new ArrayList<>();
		for (Log log : logs) {
			EventValues eventValues = Contract.staticExtractEventParameters(event, log);
			if (eventValues != null) {
				values.add(eventValues);
			}
		}
		return values;
	}

	/**
	 * get item (i,j,k...) from a multi-dimensional Web3j array
	 * @param array
	 * @param indices
	 * @return
	 */

	public static Type web3jArrayGet(Array array,int... indices){
		Array ar = array;
		Object val=null;
		for(int d=0;d<indices.length;d++){
			val = ar.getValue().get(indices[d]);
			if(d < indices.length -1)
				ar = (Array) val;
		}
		return (Type) val;
	}

	public static TransactionReceipt waitForTransactionReceipt(Web3j web3j, String txHash, long waitMsBetweenTries, int tries) throws IOException {
		try {
			return waitForTransactionReceipt(web3j, txHash, waitMsBetweenTries, tries, false);
		}
		catch(InterruptedException e){
			log.error("waitForTransactionReceipt threw InterruptedException despite throwInterruptedException = false. This shouldnt happen.");
			throw new RuntimeException(e);
		}
	}

	/**
	 *
	 * @param web3j
	 * @param txHash
	 * @param waitMsBetweenTries
	 * @param tries
	 * @param throwInterruptedException
	 * @return the TransactionReceipt, or null if none was found in allotted time
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static TransactionReceipt waitForTransactionReceipt(Web3j web3j, String txHash, long waitMsBetweenTries, int tries, boolean throwInterruptedException) throws InterruptedException, IOException {
		TransactionReceipt receipt = null;
		int retry = 0;
		while (receipt == null && retry < tries) {
			receipt = web3j.ethGetTransactionReceipt(txHash).send().getResult();
			if (receipt == null) {
				retry++;
				log.info("Couldn't get transaction receipt for tx " + txHash + ". Retry " + retry);
				try {
					Thread.sleep(waitMsBetweenTries);
				} catch (InterruptedException e) {
					log.info(e.getMessage());
					if(throwInterruptedException){
						throw e;
					}
				}
			}
		}
		return receipt;
	}

	public static Web3j getWeb3jConnectionFromConfig(){
		EthereumModuleOptions ethereumOptions = new EthereumModuleOptions();
		return Web3j.build(new HttpService(ethereumOptions.getRpcUrl()));
	}

	/**
	 * Get a public field in Ethereum contract. Returns T of a Type&lt;T&gt; specified in fieldType.
	 *
	 * For example:
	 *
	 * if contract contains:
	 * address public owner;
	 *
	 * then
	 * String s = getPublicField(web3j, contractAddress, "owner", Address.class); returns a String type with the owner address
	 *
	 * if contract contains:
	 * uint public somenum;
	 *
	 * then
	 * BigInteger i = getPublicField(web3j, contractAddress, "somenum", Uint.class); returns a BigInteger with the value of somenum
	 */
	@SuppressWarnings("unchecked")
	public static <T, X extends Type<T>> T getPublicField(Web3j web3j, String contractAddress, String fieldName, Class<X> fieldType) throws IOException {
		List<Type> input = Arrays.<Type>asList();
		List<TypeReference<?>> output = Arrays.<TypeReference<?>>asList(TypeReference.create(fieldType));
		Function func = new Function(fieldName, input, output);
		String data = FunctionEncoder.encode(func);
		Transaction tx = Transaction.createEthCallTransaction(contractAddress, contractAddress, data);
		Request<?, EthCall> request = web3j.ethCall(tx, DefaultBlockParameterName.LATEST);
		EthCall response = request.send();
		Response.Error err = response.getError();
		if (err != null) {
			throw new Web3jException(err);
		}
		List<Type> result = FunctionReturnDecoder.decode(response.getValue(), func.getOutputParameters());
		Type<X> next = result.iterator().next();
		return (T) next.getValue();
	}

	/**
	 *
	 * @param web3j
	 * @param tr
	 * @return the timestamp (seconds) of the block in which trasnaction occured, or -1 if not found
	 * @throws IOException
	 */
	public static long getBlockTime(Web3j web3j, TransactionReceipt tr) throws IOException {
		DefaultBlockParameter dbp = DefaultBlockParameter.valueOf(tr.getBlockNumber());
		EthBlock eb = web3j.ethGetBlockByNumber(dbp, false).send();
		if(eb == null){
			log.error("Error fetching block "+dbp);
			return -1;
		}
		if(eb.hasError()) {
			log.error("Error fetching block "+dbp+  ". Error = "+eb.getError());
			return -1;
		}
		long ts = eb.getBlock().getTimestamp().longValue();
		log.info("getBlockTime txHash: "+tr.getTransactionHash()+ " block number: "+tr.getBlockNumber()+ " timestamp: "+ts);
		return ts;
	}

	/**
	 * @param web3j
	 * @param erc20address   address of ERC20 or 0x0 for ETH balance
	 * @param holderAddress
	 * @return token balance in wei
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public static BigInteger getERC20Balance(Web3j web3j, String erc20address, String holderAddress) throws ExecutionException, InterruptedException {
		Address tokenAddress = new Address(erc20address);
		if (tokenAddress.toUint160().getValue().equals(BigInteger.ZERO)) {
			EthGetBalance ethGetBalance = web3j
				.ethGetBalance(holderAddress, DefaultBlockParameterName.LATEST)
				.sendAsync()
				.get();
			final BigInteger balance = ethGetBalance.getBalance();
			return balance;
		}
		//    function balanceOf(address tokenOwner) public view returns (uint balance);
		Function balanceOf = new Function("balanceOf", Arrays.<Type>asList(new Address(holderAddress)), Arrays.<TypeReference<?>>asList(TypeReference.create(Uint.class)));
		EthCall response = web3j.ethCall(
			Transaction.createEthCallTransaction(holderAddress, erc20address, FunctionEncoder.encode(balanceOf)),
			DefaultBlockParameterName.LATEST).sendAsync().get();
		Response.Error err = response.getError();
		if (err != null) {
			throw new RuntimeException(err.getMessage());
		}
		List<Type> rslt = FunctionReturnDecoder.decode(response.getValue(), balanceOf.getOutputParameters());
		return ((Uint) rslt.iterator().next()).getValue();
	}
}
