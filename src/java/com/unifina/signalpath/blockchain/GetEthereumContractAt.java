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
		addressParam.setUpdateOnChange(true);
		abiParam.setUpdateOnChange(true);
		abiParam.setCanConnect(false);
		addressParam.setDrivingInput(true);
		addressParam.canToggleDrivingInput = false;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		String address = addressParam.getValue();
		String oldAddress = (String) config.get("oldAddress");
		String abiString = MapTraversal.getString(config, "params[1].value");

		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions = EthereumModuleOptions.readFrom(options);

		// TODO: check address is valid?
		if (address.length() > 2) {
			addInput(abiParam);
			addOutput(out);

			// if address didn't change, ABI must've changed so onConfiguration is fired
			if (address.equals(oldAddress)) {
				abi = new EthereumABI(abiString);
			} else {
				// ABI param not yet added to UI => query streamr-web3 for known ABI
				try {
					String responseString = Unirest.get(ethereumOptions.getServer() + "/contract?at=" + address).asString().getBody();
					JsonObject response = new JsonParser().parse(responseString).getAsJsonObject();
					if (response.has("abi")) {
						JsonArray abiArray = response.getAsJsonArray("abi");
						abi = new EthereumABI(abiArray);
						abiParam.receive(abiArray.toString());
					}
				} catch (UnirestException e) {
					throw new RuntimeException(e);
				}
			}

			// parsing failed, ABI is empty or invalid, or etherscan didn't return anything
			if (abi == null || abi.getFunctions().size() < 1) {
				abi = new EthereumABI("[{\"type\":\"fallback\",\"payable\":true}]");
			}

			contract = new EthereumContract(address, abi);
		} else {
			contract = null;
		}
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		config.put("oldAddress", addressParam.getValue());

		if (contract != null) {
			config.put("contract", contract.toMap());
		}

		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions.writeNetworkOption(options);

		return config;
	}

	@Override
	public void sendOutput() {
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
