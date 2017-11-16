package com.unifina.signalpath.blockchain;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.ModuleOptions;
import com.unifina.signalpath.StringParameter;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Get Ethereum contract at given address
 */
public class GetEthereumContractAt extends AbstractSignalPathModule {

	public static final String DUMMY_ABI_STRING = "[{\"type\":\"fallback\",\"payable\":true}]";
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
		addressParam.setCanToggleDrivingInput(false);
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		String address = addressParam.getValue();
		String oldAddress = (String)config.get("oldAddress");

		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions = EthereumModuleOptions.readFrom(options);

		// TODO: check address is valid?
		if (address.length() == 42) {
			addInput(abiParam);
			addOutput(out);
			List<Map<String, String>> paramsList = (List)config.get("params");

			// if address didn't change, ABI must've changed so onConfiguration is fired
			if (address.equals(oldAddress)) {
				String abiString = paramsList.size() > 1 ? paramsList.get(1).get("value") : DUMMY_ABI_STRING;
				abi = new EthereumABI(abiString);
			} else {
				// ABI param not yet added to UI => query streamr-web3 for known ABI
				try {
					String abiString = "[]";
					String responseString = Unirest.get(ethereumOptions.getServer() +
							"/contract?at=" + address +
							"&network=" + ethereumOptions.getNetwork()
					).asString().getBody();
					JsonObject response = new JsonParser().parse(responseString).getAsJsonObject();
					if (response.has("abi")) {
						JsonArray abiArray = response.getAsJsonArray("abi");
						abi = new EthereumABI(abiArray);
						abiString = abiArray.toString();
					}

					if (paramsList.size() > 1) {
						paramsList.get(1).put("value", abiString);
					} else {
						abiParam.receive(abiString);
					}
				} catch (UnirestException e) {
					throw new RuntimeException(e);
				}
			}

			// parsing failed, ABI is empty or invalid, or etherscan didn't return anything
			if (abi == null || abi.getFunctions().size() < 1) {
				abi = new EthereumABI(DUMMY_ABI_STRING);
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
