package com.unifina.signalpath.blockchain;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.ModuleOptions;
import com.unifina.signalpath.StringParameter;
import com.unifina.utils.MapTraversal;
import grails.util.Holders;

import java.util.Map;

/**
 * Get Ethereum contract at given address
 */
public class GetEthereumContractAt extends AbstractSignalPathModule {

	private StringParameter addressParam = new StringParameter(this, "address", "0x");
	private StringParameter abiParam = new StringParameter(this, "ABI", "[]");
	private EthereumContractOutput out = new EthereumContractOutput(this, "contract");

	private EthereumContract contract;
	private EthereumABI abi;
	private EthereumModuleOptions ethereumOptions = new EthereumModuleOptions();

	@Override
	public void init() {
		addInput(addressParam);
		addInput(abiParam);
		abiParam.setUpdateOnChange(true);
		abiParam.setCanConnect(false);
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		String address = addressParam.getValue();
		String abiString = MapTraversal.getString(config, "params[1].value");

		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions = EthereumModuleOptions.readFrom(options);

		abi = new EthereumABI(abiString);

		// TODO: add etherscan lookup

		// parsing failed, ABI is empty or invalid, or etherscan didn't return anything
		if (abi == null || abi.getFunctions().size() < 1) {
			abi = new EthereumABI("[{\"type\":\"fallback\",\"payable\":true}]");
		}

		// TODO: check address is valid? Use web3j utils?
		if (address.length() > 2) {
			addOutput(out);
			contract = new EthereumContract(address, abi);
		} else {
			contract = null;
		}
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		if (contract != null) {
			config.put("contract", contract.toMap());
		}

		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions.writeNetworkOption(options);

		return config;
	}

	/**
	 * GetContractAt may be activated during run-time when the address is updated.
	 * The resulting contract should point at the updated address.
	 */
	@Override
	public void sendOutput() {
		String abiString = abiParam.getValue();
		EthereumABI abi = new EthereumABI(abiString);

		if (contract != null && !addressParam.getValue().equals(contract.getAddress()) && abi != null) {
			contract = new EthereumContract(addressParam.getValue(), abi);
		}

		if (contract != null) {
			out.send(contract);
		}
	}

	@Override
	public void clearState() {

	}
}
