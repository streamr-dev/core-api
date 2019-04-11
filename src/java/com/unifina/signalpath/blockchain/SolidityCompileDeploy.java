package com.unifina.signalpath.blockchain;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.unifina.data.FeedEvent;
import com.unifina.signalpath.*;
import com.unifina.utils.MapTraversal;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.ethereum.solidity.compiler.Solc;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SolidityCompileDeploy extends ModuleWithUI implements Pullable<EthereumContract> {

	private static final Logger log = Logger.getLogger(SolidityCompileDeploy.class);

	private final EthereumAccountParameter ethereumAccount = new EthereumAccountParameter(this, "ethAccount");
	private Output<EthereumContract> contractOutput = null;

	private String code = null;
	private EthereumContract contract = null;
	private DoubleParameter sendEtherParam = new DoubleParameter(this, "initial ETH", 0.0);

	private EthereumModuleOptions ethereumOptions = new EthereumModuleOptions();
	private Web3j web3j;
	@Override
	public void init() {
		addInput(ethereumAccount);
		ethereumAccount.setUpdateOnChange(true);
	}

	@Override
	public void sendOutput() {

	}

	@Override
	public void clearState() {

	}

	/** Override to provide contract template that will be compiled when module is added to canvas */
	public String getCodeTemplate() {
		return null;
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions.writeTo(options);

		config.put("code", code);
		if (contract != null) {
			config.put("contract", contract.toMap());
		}

		return config;
	}

	protected SolidityCompiler.Options[] getSolcOptions(){
		//List<SolidityCompiler.Options> opts = new ArrayList<SolidityCompiler.Options>();
		SolidityCompiler.Options[] opts = {SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN};
		return opts;
	}

	protected JsonObject compile(String solidity_code) throws IOException {
		SolidityCompiler.Result rslt = SolidityCompiler.compile(solidity_code.getBytes(StandardCharsets.UTF_8),true,getSolcOptions());
		if(rslt.isFailed()){
			throw new IOException("Error compiling contract: " + rslt.errors);
		}
		JsonParser parser = new JsonParser();
		Set<Map.Entry<String,JsonElement>> entries = parser.parse(rslt.output).getAsJsonObject().get("contracts").getAsJsonObject().entrySet();
		Map.Entry<String,JsonElement> e = entries.iterator().next();
		return e.getValue().getAsJsonObject();
	}

	protected BigInteger getGasLimit(){
		return BigInteger.valueOf(6000000l);
	}
	protected Function encodeConstructor(List<Object> args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		EthereumABI.Function constructor = contract.getABI().getConstructor();
		ArrayList<String> solidity_inputs = new ArrayList<String>(constructor.inputs.size());
		ArrayList<String> solidity_outputs = new ArrayList<String>(constructor.outputs.size());
		for(EthereumABI.Slot s : constructor.inputs)
			solidity_inputs.add(s.type);
		for(EthereumABI.Slot s : constructor.outputs)
			solidity_outputs.add(s.type);
		return Web3jHelper.encodeFunction(constructor.name,solidity_inputs,args,solidity_outputs);
	}


	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		web3j = Web3j.build(new HttpService(ethereumOptions.getRpcUrl()));

		Object codeFromEditor = config.get("code");
		if (codeFromEditor == null) {
			code = getCodeTemplate();			// initially, get template from inheriting class e.g. PayByUse
		} else {
			code = codeFromEditor.toString();
		}

		if (config.containsKey("contract")) {
			contract = EthereumContract.fromMap(MapTraversal.getMap(config, "contract"));
		}

		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions = EthereumModuleOptions.readFrom(options);
		//final StreamrWeb3Interface web3 = createWeb3Interface();
		try {
			JsonParser parser = new JsonParser();
			if (config.containsKey("compile") || (code != null && !code.trim().isEmpty() && ethereumAccount.getAddress() != null && (contract == null || !contract.isDeployed()))) {
				JsonObject jsonTree = compile(code);
				contract = new EthereumContract(null, new EthereumABI(parser.parse(jsonTree.get("abi").getAsString()).getAsJsonArray()));
//				contract = web3.compile(code);
			}
			if (config.containsKey("deploy")) {
				if(contract != null && contract.isDeployed())
					return;
				JsonObject jsonTree = compile(code);
				// Make sure the contract is compiled
				if (contract == null) {
					contract = new EthereumContract(null, new EthereumABI(parser.parse(jsonTree.get("abi").getAsString()).getAsJsonArray()));
				}
				EthereumABI.Function constructor = contract.getABI().getConstructor();
				BigInteger sendWei = BigInteger.ZERO;
				Stack<Object> args = new Stack<>();
				if (constructor != null) {
					List<Map> params = (List) config.get("params");
					// skip first parameter (ethAccount, not constructor parameter)
					for (Map param : params.subList(1, params.size())) {
						args.push(param.get("value"));
					}
					// for payable constructors, sendEtherParam is added in params after the ordinary function arguments
					// value can't be read from sendEtherParam.getValue because it's not added to the module until createParameters is called (so it exists in config but not in module object)
					if (constructor.payable) {
						BigDecimal sendEtherParamValue = new BigDecimal(args.pop().toString());
						sendWei = sendEtherParamValue.multiply(BigDecimal.TEN.pow(18)).toBigInteger();
					}
				}

				BigInteger gasPrice = BigDecimal.valueOf(ethereumOptions.getGasPriceWei()).toBigInteger();
				EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
						ethereumAccount.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
				BigInteger nonce = ethGetTransactionCount.getTransactionCount();
				Credentials credentials = Credentials.create(ethereumAccount.getPrivateKey());

				//createTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, BigInteger value, String data) {
				Function cons = encodeConstructor(args);
				String cons_encoded = FunctionEncoder.encodeConstructor(cons.getInputParameters());
				String createContract  = jsonTree.get("bin").getAsString()+ cons_encoded;
				RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, getGasLimit(), null, sendWei, createContract);
				byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
				String hexValue = Numeric.toHexString(signedMessage);

				//async deploy not implemented atm because of UI constraints:
				/*
				CompletableFuture<EthSendTransaction> cf = web3j.ethSendRawTransaction(hexValue).sendAsync();
				cf.thenAccept(new Consumer<EthSendTransaction>() {
					@Override
					public void accept(EthSendTransaction tx) {
					}
				});
				*/
				//synchronous deploy:
				EthSendTransaction tx = web3j.ethSendRawTransaction(hexValue).send();
				String txhash = tx.getTransactionHash();
				log.debug("TX response: " + txhash);
				String address = web3j.ethGetTransactionReceipt(txhash).send().getResult().getContractAddress();
				contract = new EthereumContract(address,contract.getABI());
			}
		} catch (Exception e) {
			if (ExceptionUtils.getRootCause(e) instanceof java.net.ConnectException) {
				log.error("Could not connect to web3 backend!", e);
				throw new RuntimeException("Sorry, we couldn't contact Ethereum at this time. We'll try to fix this soon.");
			} else {
				throw new RuntimeException(e);
			}
		}

		if (contract != null) {
			createContractOutput();
			createParameters(contract.getABI());
		}
	}
	
	protected StreamrWeb3Interface createWeb3Interface() {
		return new StreamrWeb3Interface(ethereumOptions.getServer(), ethereumOptions.getGasPriceWei());
	}

	private void createContractOutput() {
		contractOutput = new EthereumContractOutput(this, "contract");
		addOutput(contractOutput);
	}

	private void createParameters(EthereumABI abi) {
		EthereumABI.Function constructor = abi.getConstructor();
		if (constructor != null) {
			for (EthereumABI.Slot input : constructor.inputs) {
				String name = input.name.replace("_", " ");
				Parameter p = EthereumToStreamrTypes.asParameter(input.type, name, this);
				p.setCanConnect(false);
				p.setCanToggleDrivingInput(false);
				addInput(p);
			}
			if (constructor.payable) {
				addInput(sendEtherParam);
			}
		}
	}

	@Override
	public EthereumContract pullValue(Output output) {
		return contract;
	}

	@Override
	public void connectionsReady() {
		super.connectionsReady();
		if (contract != null) {
			contractOutput.send(contract);
		}
	}

}
