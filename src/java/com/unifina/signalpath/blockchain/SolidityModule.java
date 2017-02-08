package com.unifina.signalpath.blockchain;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mashape.unirest.http.Unirest;
import com.unifina.signalpath.*;
import com.unifina.utils.MapTraversal;
import grails.util.Holders;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.net.ConnectException;
import java.util.List;
import java.util.Map;

public class SolidityModule extends ModuleWithUI implements Pullable<EthereumContract> {

	public static final String ETH_SERVER_URL = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.server");
	private static final Logger log = Logger.getLogger(SolidityModule.class);

	private Output<EthereumContract> contractOutput = null;

	private String code = null;
	private EthereumContract contract = null;
	private boolean deployed = false;

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

		config.put("code", code);
		config.put("deployed", deployed);
		if (contract != null) {
			config.put("contract", contract.toMap());
		}

		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		code = config.containsKey("code") ? config.get("code").toString() : getCodeTemplate();

		if (config.containsKey("contract")) {
			contract = EthereumContract.fromMap(MapTraversal.getMap(config, "contract"));
		}
		if (config.containsKey("deployed")) {
			deployed = MapTraversal.getBoolean(config, "deployed");
		}

		try {
			if (config.containsKey("compile") && code != null) {
				contract = compile(code);
			} else if (config.containsKey("deploy") && !contract.isDeployed()) {
				contract = deploy(code);
				createContractOutput();
				deployed = true;
			}
		} catch (Exception e) {
			if (ExceptionUtils.getRootCause(e) instanceof java.net.ConnectException){
				log.error("Could not connect to web3 backend!", e);
				throw new RuntimeException("Sorry, we couldn't contact Ethereum at this time. We'll try to fix this soon.");
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
				Parameter p = EthereumToStreamrTypes.asParameter(input.type, input.name, this);
				p.setCanConnect(false);
				p.canToggleDrivingInput = false;
				p.canBeFeedback = false;
				addInput(p);
			}
		}
	}

	/** @returns EthereumContract with isDeployed() false */
	private static EthereumContract compile(String code) throws Exception {
		return getContractFrom(ETH_SERVER_URL + "/compile", code);
	}

	/** @returns EthereumContract that isDeployed() */
	private static EthereumContract deploy(String code) throws Exception {
		return getContractFrom(ETH_SERVER_URL + "/deploy", code);
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

	private static EthereumContract getContractFrom(String url, String code) throws Exception {
		String responseJson = Unirest.post(url).body(code).asString().getBody();
		CompileResponse returned = new Gson().fromJson(responseJson, CompileResponse.class);
		if (returned.contracts.size() > 0) {
			// TODO: bring returned.errors to UI somehow? They're warnings probably since compilation was successful
			// TODO: handle several contracts returned?
			ContractMetadata c = returned.contracts.get(0);
			return new EthereumContract(c.address, new EthereumABI(c.abi));
		} else {
			// TODO java 8: String.join
			throw new RuntimeException(new Gson().toJson(returned.errors));
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
