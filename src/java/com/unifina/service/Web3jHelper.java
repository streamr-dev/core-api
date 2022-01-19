package com.unifina.service;

import com.unifina.utils.ThreadUtil;
import org.apache.log4j.Logger;
import org.web3j.abi.EventValues;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.tx.Contract;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public final class Web3jHelper {
	private static final Logger log = Logger.getLogger(Web3jHelper.class);

	private Web3jHelper() {
	}

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

	public static List<EventValues> extractEventParameters(Event event, TransactionReceipt transactionReceipt) {
		List<Log> logs = transactionReceipt.getLogs();
		return extractEventParameters(event, logs);
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
	 */
	public static Type web3jArrayGet(Array array, int... indices) {
		Array ar = array;
		Object val = null;
		for (int d = 0; d < indices.length; d++) {
			val = ar.getValue().get(indices[d]);
			if (d < indices.length - 1) {
				ar = (Array) val;
			}
		}
		return (Type) val;
	}

	/**
	 * @return the TransactionReceipt, or null if none was found in allotted time
	 */
	public static TransactionReceipt waitForTransactionReceipt(Web3j web3j, String txHash, long waitMsBetweenTries, int tries) throws IOException {
		TransactionReceipt receipt = null;
		int retry = 0;
		while (receipt == null && retry < tries) {
			receipt = web3j.ethGetTransactionReceipt(txHash).send().getResult();
			if (receipt == null) {
				retry++;
				log.info("Couldn't get transaction receipt for tx " + txHash + ". Retry " + retry);
				ThreadUtil.sleep(waitMsBetweenTries);
			}
		}
		return receipt;
	}

	/**
	 * Get a public field in Ethereum contract. Returns T of a Type&lt;T&gt; specified in fieldType.
	 *
	 * <p>
	 * For example:
	 * <pre>
	 * if contract contains:
	 * address public owner;
	 *
	 * then
	 * String s = getPublicField(web3j, contractAddress, "owner", Address.class);
	 * returns a String type with the owner address
	 *
	 * if contract contains:
	 * uint public somenum;
	 *
	 * then
	 * BigInteger i = getPublicField(web3j, contractAddress, "somenum", Uint.class);
	 * returns a BigInteger with the value of somenum
	 * </pre>
	 *
	 * @return Web3j.Type return value from eth_call (Solidity type + value), or null if bad address or fieldName
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
			String msg = String.format("getPublicField() error: message: '%s', code: '%d', data: '%s', contract address: '%s'", err.getMessage(), err.getCode(), err.getData(), contractAddress);
			log.error(msg);
			throw new BlockchainException(msg);
		}
		List<Type> result = FunctionReturnDecoder.decode(response.getValue(), func.getOutputParameters());
		Iterator<Type> i = result.iterator();
		if (i.hasNext()) {
			Type<X> next = i.next();
			return (T) next.getValue();
		}
		// "Note: If an invalid function call is made, or a null result is obtained, the return value will be an instance of Collections.emptyList()" https://web3j.readthedocs.io/en/latest/transactions.html
		log.info(String.format("public field '%s' not found in contract '%s'", fieldName, contractAddress));
		return null;
	}

	/**
	 * @return the timestamp (seconds) of the block in which trasnaction occured
	 */
	public static long getBlockTime(Web3j web3j, TransactionReceipt tr) throws IOException, BlockchainException {
		DefaultBlockParameter dbp = DefaultBlockParameter.valueOf(tr.getBlockNumber());
		EthBlock eb = web3j.ethGetBlockByNumber(dbp, false).send();
		if (eb == null) {
			throw new IOException("Error sending ethGetBlockByNumber for block " + dbp);
		}
		if (eb.hasError()) {
			String err = "Error fetching block " + dbp + ". Error = " + eb.getError();
			log.error(err);
			throw new BlockchainException(err);
		}
		EthBlock.Block block = eb.getBlock();
		if (block == null) {
			String err = "getBlock() returned null block for txHash " + tr.getTransactionHash();
			log.error(err);
			throw new BlockchainException(err);
		}
		BigInteger timestamp = block.getTimestamp();
		if (timestamp == null) {
			String err = "getBlock() returned null timestamp for block: " + block + ", re txHash: " + tr.getTransactionHash();
			log.error(err);
			throw new BlockchainException(err);
		}

		long ts = timestamp.longValue();
		log.info("getBlockTime txHash: " + tr.getTransactionHash() + " block number: " + tr.getBlockNumber() + " timestamp: " + ts);
		return ts;
	}

	/**
	 * @param erc20address address of ERC20 or 0x0 for ETH balance
	 * @return token balance in wei
	 */
	public static BigInteger getERC20Balance(Web3j web3j, String erc20address, String holderAddress) throws ExecutionException, InterruptedException {
		Address tokenAddress = new Address(erc20address);
		if (tokenAddress.toUint().getValue().equals(BigInteger.ZERO)) {
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
