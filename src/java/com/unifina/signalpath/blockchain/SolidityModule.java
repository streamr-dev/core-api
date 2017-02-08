package com.unifina.signalpath.blockchain;

import com.amazonaws.services.simpleworkflow.model.Run;
import com.google.gson.*;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.unifina.signalpath.ModuleWithUI;
import com.unifina.signalpath.Output;
import com.unifina.signalpath.Parameter;
import com.unifina.signalpath.Pullable;
import com.unifina.utils.MapTraversal;

import java.util.List;
import java.util.Map;

public class SolidityModule extends ModuleWithUI implements Pullable<EthereumContract> {

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

		if (config.containsKey("code")) {
			code = MapTraversal.getString(config, "code");
		}
		if (config.containsKey("contract")) {
			contract = EthereumContract.fromMap(MapTraversal.getMap(config, "contract"));
		}
		if (config.containsKey("deployed")) {
			deployed = MapTraversal.getBoolean(config, "deployed");
		}

		try {
			if (deployed && !contract.isDeployed()) {
				contract = deploy(code);
				createContractOutput();
			} else if (code != null) {
				contract = compile(code);
				createContractOutput();
				createParameters(contract.getABI());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
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
		return getContractFrom("http://localhost:3000/compile", code);
	}

	/** @returns EthereumContract that isDeployed() */
	private static EthereumContract deploy(String code) throws Exception {
		return getContractFrom("http://localhost:3000/deploy", code);
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
