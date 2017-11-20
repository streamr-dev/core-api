package com.unifina.signalpath.blockchain;

import com.amazonaws.services.simpleworkflow.model.Run;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
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

	// from https://github.com/web3j/web3j/pull/134/files
	// TODO: move to EthereumContract?
	private static final Pattern ignoreCaseAddrPattern = Pattern.compile("(?i)^(0x)?[0-9a-f]{40}$");
	public static boolean isValidAddress(String address) {
		return ignoreCaseAddrPattern.matcher(address).find();
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		String address = addressParam.getValue();
		String oldAddress = (String)config.get("oldAddress");

		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions = EthereumModuleOptions.readFrom(options);

		if (!isValidAddress(address)) {
			contract = null;
			return;
		}

		addInput(abiParam);
		addOutput(out);
		List<Map<String, String>> paramsList = (List)config.get("params");
		RuntimeException abiError = null;

		// if address didn't change, ABI must've changed so that onConfiguration gets fired in the first place
		if (address.equals(oldAddress)) {
			if (paramsList.size() > 1) {
				try {
					String abiString = paramsList.get(1).get("value");
					abi = new EthereumABI(abiString);
				} catch (RuntimeException e) {
					abi = null;
					abiError = e;
				}
			}
		} else {
			// Address changed => query streamr-web3 for known ABI
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
				} else {
					abi = null;
				}

				// change ABI in the UI; check if ABI parameter was shown yet
				if (paramsList.size() > 1) {
					paramsList.get(1).put("value", abiString);
				} else {
					abiParam.receive(abiString);
				}
			} catch (UnirestException e) {
				abi = null;
				abiError = new RuntimeException(e);
			}
		}

		// parsing failed, ABI is empty or invalid, or etherscan didn't return anything
		if (abi == null || abi.getFunctions().size() < 1) {
			abi = new EthereumABI(DUMMY_ABI_STRING);
		}

		contract = new EthereumContract(address, abi);

		if (abiError != null) {
			throw abiError;
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
