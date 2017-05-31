package com.unifina.signalpath.blockchain;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.Unirest;
import com.unifina.signalpath.*;
import com.unifina.signalpath.blockchain.templates.EthereumModuleOptions;
import com.unifina.utils.MapTraversal;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SolidityModule extends ModuleWithUI implements Pullable<EthereumContract> {

	private static final Logger log = Logger.getLogger(SolidityModule.class);
	protected static final String ADDRESS_PLACEHOLDER = "{{ADDRESS}}";

	private final EthereumAccountParameter ethereumAccount = new EthereumAccountParameter(this, "ethAccount");
	private Output<EthereumContract> contractOutput = null;

	private EthereumModuleOptions ethereumOptions = new EthereumModuleOptions();
	private String code = null;
	private EthereumContract contract = null;
	private DoubleParameter sendEtherParam = new DoubleParameter(this, "initial ETH", 0.0);

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

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		boolean compileRequested = config.containsKey("compile");

		if (config.containsKey("code")) {
			code = config.get("code").toString();
		} else {
			code = getCodeTemplate();
			compileRequested = true;
		}

		if (config.containsKey("contract")) {
			contract = EthereumContract.fromMap(MapTraversal.getMap(config, "contract"));
		}

		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions = EthereumModuleOptions.readFrom(options);

		try {
			if (compileRequested && code != null) {
				contract = compile(code);
			} else if (config.containsKey("deploy") && !contract.isDeployed()) {
				EthereumABI.Function constructor = contract.getABI().getConstructor();
				String sendWei = "0";
				Stack<Object> args = new Stack<>();

				if (constructor != null) {
					List<Map> params = (List)config.get("params");
					for (Map param : params) {
						args.push(param.get("value"));
					}
					// for payable constructors, sendEtherParam is added in params after the ordinary function arguments
					if (constructor.payable) {
						BigDecimal valueWei = BigDecimal.valueOf(sendEtherParam.getValue()).multiply(BigDecimal.TEN.pow(18));
						sendWei = valueWei.toBigInteger().toString();
						args.pop();
					}
				}

				contract = deploy(code, args, sendWei);
			}
		} catch (Exception e) {
			// TODO: currently I got no notification when URL was incorrect
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
				p.canToggleDrivingInput = false;
				p.canBeFeedback = false;
				addInput(p);
			}
			if (constructor.payable) {
				addInput(sendEtherParam);
			}
		}
	}

	private String replaceDynamicFields(String code) {
		return code.replace(ADDRESS_PLACEHOLDER, ethereumAccount.getAddress());
	}

	/** @returns EthereumContract with isDeployed() false */
	private EthereumContract compile(String code) throws Exception {
		code = replaceDynamicFields(code);

		String bodyJson = new Gson().toJson(ImmutableMap.of(
			"code", code
		)).toString();

		log.info("compile request: "+bodyJson);

		String responseJson = Unirest.post(ethereumOptions.getServer() + "/compile")
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.body(bodyJson)
				.asString()
				.getBody();

		CompileResponse returned;
		try {
			returned = new Gson().fromJson(responseJson, CompileResponse.class);
		} catch (Exception e) {
			log.error("Error parsing JSON response from Ethereum backend. Response was: \n "+ responseJson, e);
			throw e;
		}

		log.info("compile response: "+responseJson);

		if (returned.contracts != null && returned.contracts.size() > 0) {
			// TODO: bring returned.errors to UI somehow? They're warnings probably since compilation was successful
			// TODO: handle several contracts returned?
			ContractMetadata c = returned.contracts.get(0);
			return new EthereumContract(c.address, new EthereumABI(c.abi));
		} else {
			// TODO java 8: String.join
			throw new RuntimeException(new Gson().toJson(returned.errors));
		}
	}

	/**
	 * @param sendWei String representation of decimal value of wei to send
	 * @returns EthereumContract that isDeployed()
	 **/
	private EthereumContract deploy(String code, List<Object> args, String sendWei) throws Exception {
		code = replaceDynamicFields(code);

		String bodyJson = new Gson().toJson(ImmutableMap.of(
			"source", ethereumAccount.getAddress(),
			"key", ethereumAccount.getPrivateKey(),
			"code", code,
			"args", args,
			"value", sendWei
		)).toString();

		Unirest.setTimeouts(10*1000, 10*60*1000); // wait patiently for the next mined block, up to 10 minutes

		log.info("deploy request: "+bodyJson);

		String responseJson = Unirest.post(ethereumOptions.getServer() + "/deploy")
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.body(bodyJson)
				.asString()
				.getBody();

		log.info("deploy response: "+responseJson);

		CompileResponse returned;
		try {
			returned = new Gson().fromJson(responseJson, CompileResponse.class);
		} catch (Exception e) {
			log.error("Error parsing JSON response from Ethereum backend. Response was: \n "+ responseJson, e);
			JsonObject response = new JsonParser().parse(responseJson).getAsJsonObject();
			if (response.get("error") != null) {
				throw new RuntimeException(response.get("error").toString());
			} else if (response.get("errors") != null) {
				throw new RuntimeException(response.get("errors").toString());
			} else {
				throw e;
			}
		}

		if (returned.contracts != null && returned.contracts.size() > 0) {
			// TODO: bring returned.errors to UI somehow? They're warnings probably since compilation was successful
			// TODO: handle several contracts returned?
			ContractMetadata c = returned.contracts.get(0);
			return new EthereumContract(c.address, new EthereumABI(c.abi));
		} else {
			// TODO java 8: String.join
			throw new RuntimeException(new Gson().toJson(returned.errors));
		}
	}

	private static class CompileResponse {
		List<ContractMetadata> contracts;
		List<String> errors;
	}

	private static class ContractMetadata {
		String name;
		String bytecode;
		JsonArray abi;
		String address;
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
