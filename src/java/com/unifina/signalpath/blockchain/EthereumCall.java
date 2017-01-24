package com.unifina.signalpath.blockchain;


import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.StringOutput;
import com.unifina.signalpath.StringParameter;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Send out a call to specified function in Ethereum block chain
 */
public class EthereumCall extends AbstractSignalPathModule {

	private static final Logger log = Logger.getLogger(EthereumCall.class);

	private StringParameter address = new StringParameter(this, "address", "");
	private StringParameter method = new StringParameter(this, "method", "");

	private StringOutput transactionHash = new StringOutput(this, "transactionHash");

	private List<Input<Object>> arguments = new ArrayList<>();

	private String currentAddress = "";
	private boolean isValid = false;
	/*
	private Web3j web3;
	private Credentials credentials;
	*/
	@Override
	public void init() {
		addInput(address);
		addInput(method);
		address.setUpdateOnChange(true);
		method.setUpdateOnChange(true);

		addOutput(transactionHash);
	}

	@Override
	public void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		if (!currentAddress.equals(address.getValue())) {
			currentAddress = address.getValue();
			log.debug("Reading ABI (argument count and types) from Ethereum contract " + currentAddress);
			int argCount = 1;
			arguments = new ArrayList<>(argCount);
			for (int i = 0; i < argCount; i++) {
				String name = "test";
				String type = "Object";
				Input<Object> input = new Input<>(this, name, type);
				addInput(input);
				arguments.add(input);
			}
			isValid = true;
		}
	}

	@Override
	public void clearState() {
		currentAddress = "";
		isValid = false;
	}

	@Override
	public void sendOutput() {
		if (!isValid) {
			log.error("Module activated in bad state!");
			return;
		}

		log.debug("Sending function call to Ethereum contract " + currentAddress);
		//for (int i = 0; i < arguments.size(); i++) { Input input = arguments[i];
		for (Input<Object> input : arguments) {
			String name = input.getName();
			String value = input.getValue().toString();
			log.debug("  " + name + ": " + value);
		}

		String sourceAddress = "0xb3428050ea2448ed2e4409be47e1a50ebac0b2d2";
		String targetAddress = address.toString();

		try {
			/*
			if (web3 == null) {
				// On Java 8: Web3j.build
				web3 = new JsonRpc2_0Web3j(new HttpService());
				//credentials = WalletUtils.loadCredentials("password", "/path/to/walletfile");
				credentials = Credentials.create("6e340f41a1c6e03e6e0a4e9805d1cea342f6a299e7c931d6f3da6dd34cb6e17d");
			}

			Function function = new Function(
					"owner",
					Arrays.<Type>asList(),
					Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
			String encodedFunction = FunctionEncoder.encode(function);

			Request r = web3.ethCall(Transaction.createEthCallTransaction(targetAddress, encodedFunction), DefaultBlockParameterName.LATEST);
			Response<EthCall> resp = r.send();

			EthCall res = resp.getResult();
			String resValue = res.getValue();

			transactionHash.send(resValue);
			*/

			/*
			// get the next available nonce
			EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(sourceAddress, DefaultBlockParameterName.LATEST).send();
			BigInteger nonce = ethGetTransactionCount.getTransactionCount();

			// create our transaction
			String data = "lol";
			RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, GAS_PRICE, GAS_LIMIT, targetAddress, data);

			// sign & send our transaction
			byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
			String hexValue = Hex.encodeHexString(signedMessage);
			EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).send();
			String txHash = ethSendTransaction.getTransactionHash();

			transactionHash.send(txHash);
			*/

		} catch (IOException e) {
			log.error(e);
		}
	}

}
