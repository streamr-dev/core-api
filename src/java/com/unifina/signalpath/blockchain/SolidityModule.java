package com.unifina.signalpath.blockchain;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.unifina.signalpath.ModuleWithUI;
import com.unifina.signalpath.Output;
import com.unifina.signalpath.Parameter;
import com.unifina.signalpath.Pullable;
import com.unifina.utils.MapTraversal;

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

		if (deployed && !contract.isDeployed()) {
			contract = deploy(code);
			createContractOutput();
		} else if (code != null) {
			contract = compile(code);
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

	private static EthereumContract compile(String code) {
		// TODO: api call to compile code and get abi as json in return

		return new EthereumContract(new EthereumABI("\n" +
				"\t [{\n" +
				"\t\"constant\": true,\n" +
				"\t\"inputs\": [],\n" +
				"\t\"name\": \"weiPerUnit\",\n" +
				"\t\"outputs\": [{\n" +
				"\t\t\"name\": \"\",\n" +
				"\t\t\"type\": \"uint256\"\n" +
				"\t}],\n" +
				"\t\"payable\": false,\n" +
				"\t\"type\": \"function\"\n" +
				"}, {\n" +
				"\t\"constant\": false,\n" +
				"\t\"inputs\": [],\n" +
				"\t\"name\": \"withdraw\",\n" +
				"\t\"outputs\": [],\n" +
				"\t\"payable\": false,\n" +
				"\t\"type\": \"function\"\n" +
				"}, {\n" +
				"\t\"constant\": true,\n" +
				"\t\"inputs\": [],\n" +
				"\t\"name\": \"STREAMR_ADDRESS\",\n" +
				"\t\"outputs\": [{\n" +
				"\t\t\"name\": \"\",\n" +
				"\t\t\"type\": \"address\"\n" +
				"\t}],\n" +
				"\t\"payable\": false,\n" +
				"\t\"type\": \"function\"\n" +
				"}, {\n" +
				"\t\"constant\": true,\n" +
				"\t\"inputs\": [],\n" +
				"\t\"name\": \"recipient\",\n" +
				"\t\"outputs\": [{\n" +
				"\t\t\"name\": \"\",\n" +
				"\t\t\"type\": \"address\"\n" +
				"\t}],\n" +
				"\t\"payable\": false,\n" +
				"\t\"type\": \"function\"\n" +
				"}, {\n" +
				"\t\"constant\": true,\n" +
				"\t\"inputs\": [],\n" +
				"\t\"name\": \"unpaidWei\",\n" +
				"\t\"outputs\": [{\n" +
				"\t\t\"name\": \"\",\n" +
				"\t\t\"type\": \"uint256\"\n" +
				"\t}],\n" +
				"\t\"payable\": false,\n" +
				"\t\"type\": \"function\"\n" +
				"}, {\n" +
				"\t\"constant\": false,\n" +
				"\t\"inputs\": [{\n" +
				"\t\t\"name\": \"addedUnits\",\n" +
				"\t\t\"type\": \"uint256\"\n" +
				"\t}],\n" +
				"\t\"name\": \"update\",\n" +
				"\t\"outputs\": [],\n" +
				"\t\"payable\": false,\n" +
				"\t\"type\": \"function\"\n" +
				"}, {\n" +
				"\t\"inputs\": [{\n" +
				"\t\t\"name\": \"_recipient\",\n" +
				"\t\t\"type\": \"address\"\n" +
				"\t}, {\n" +
				"\t\t\"name\": \"_weiPerUnit\",\n" +
				"\t\t\"type\": \"uint256\"\n" +
				"\t}],\n" +
				"\t\"payable\": true,\n" +
				"\t\"type\": \"constructor\"\n" +
				"}, {\n" +
				"\t\"payable\": true,\n" +
				"\t\"type\": \"fallback\"\n" +
				"}, {\n" +
				"\t\"anonymous\": false,\n" +
				"\t\"inputs\": [{\n" +
				"\t\t\"indexed\": false,\n" +
				"\t\t\"name\": \"debt\",\n" +
				"\t\t\"type\": \"uint256\"\n" +
				"\t}],\n" +
				"\t\"name\": \"OutOfFunds\",\n" +
				"\t\"type\": \"event\"\n" +
				"}]"));
	}


	private static EthereumContract deploy(String code) {
		// TODO: api call
		return new EthereumContract("address", new EthereumABI("\n" +
				"\t [{\n" +
				"\t\"constant\": true,\n" +
				"\t\"inputs\": [],\n" +
				"\t\"name\": \"weiPerUnit\",\n" +
				"\t\"outputs\": [{\n" +
				"\t\t\"name\": \"\",\n" +
				"\t\t\"type\": \"uint256\"\n" +
				"\t}],\n" +
				"\t\"payable\": false,\n" +
				"\t\"type\": \"function\"\n" +
				"}, {\n" +
				"\t\"constant\": false,\n" +
				"\t\"inputs\": [],\n" +
				"\t\"name\": \"withdraw\",\n" +
				"\t\"outputs\": [],\n" +
				"\t\"payable\": false,\n" +
				"\t\"type\": \"function\"\n" +
				"}, {\n" +
				"\t\"constant\": true,\n" +
				"\t\"inputs\": [],\n" +
				"\t\"name\": \"STREAMR_ADDRESS\",\n" +
				"\t\"outputs\": [{\n" +
				"\t\t\"name\": \"\",\n" +
				"\t\t\"type\": \"address\"\n" +
				"\t}],\n" +
				"\t\"payable\": false,\n" +
				"\t\"type\": \"function\"\n" +
				"}, {\n" +
				"\t\"constant\": true,\n" +
				"\t\"inputs\": [],\n" +
				"\t\"name\": \"recipient\",\n" +
				"\t\"outputs\": [{\n" +
				"\t\t\"name\": \"\",\n" +
				"\t\t\"type\": \"address\"\n" +
				"\t}],\n" +
				"\t\"payable\": false,\n" +
				"\t\"type\": \"function\"\n" +
				"}, {\n" +
				"\t\"constant\": true,\n" +
				"\t\"inputs\": [],\n" +
				"\t\"name\": \"unpaidWei\",\n" +
				"\t\"outputs\": [{\n" +
				"\t\t\"name\": \"\",\n" +
				"\t\t\"type\": \"uint256\"\n" +
				"\t}],\n" +
				"\t\"payable\": false,\n" +
				"\t\"type\": \"function\"\n" +
				"}, {\n" +
				"\t\"constant\": false,\n" +
				"\t\"inputs\": [{\n" +
				"\t\t\"name\": \"addedUnits\",\n" +
				"\t\t\"type\": \"uint256\"\n" +
				"\t}],\n" +
				"\t\"name\": \"update\",\n" +
				"\t\"outputs\": [],\n" +
				"\t\"payable\": false,\n" +
				"\t\"type\": \"function\"\n" +
				"}, {\n" +
				"\t\"inputs\": [{\n" +
				"\t\t\"name\": \"_recipient\",\n" +
				"\t\t\"type\": \"address\"\n" +
				"\t}, {\n" +
				"\t\t\"name\": \"_weiPerUnit\",\n" +
				"\t\t\"type\": \"uint256\"\n" +
				"\t}],\n" +
				"\t\"payable\": true,\n" +
				"\t\"type\": \"constructor\"\n" +
				"}, {\n" +
				"\t\"payable\": true,\n" +
				"\t\"type\": \"fallback\"\n" +
				"}, {\n" +
				"\t\"anonymous\": false,\n" +
				"\t\"inputs\": [{\n" +
				"\t\t\"indexed\": false,\n" +
				"\t\t\"name\": \"debt\",\n" +
				"\t\t\"type\": \"uint256\"\n" +
				"\t}],\n" +
				"\t\"name\": \"OutOfFunds\",\n" +
				"\t\"type\": \"event\"\n" +
				"}]"));
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
