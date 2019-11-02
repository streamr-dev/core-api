package com.unifina.signalpath.blockchain;

import com.google.gson.*;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.ModuleOptions;
import com.unifina.signalpath.StringParameter;
import com.unifina.utils.MapTraversal;
import org.apache.log4j.Logger;
import org.web3j.utils.Numeric;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Get Ethereum contract at given address
 */
public class GetEthereumContractAt extends AbstractSignalPathModule {
	private static final Logger log = Logger.getLogger(GetEthereumContractAt.class);

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
		addInput(abiParam);
		abiParam.setUpdateOnChange(true);
		abiParam.setCanConnect(false);
	}

	/**
	 * Example: https://api.etherscan.io/api?module=contract&action=getabi&address=0xA10151D088f6f2705a05d6c83719e99E079A61C1
	 * @param network name: "homestead"="mainnet"=null, "rinkeby", "ropsten", "kovan", "goerli"
	 * @param address of the contract (ENS name doesn't work as for 2019-11-02)
	 * @return URL for fetching the ABI for a contract from Etherscan
	 */
	public static String getEtherscanAbiQueryUrl(String network, String address) {
		boolean useMainNet = network == null || network.startsWith("main") || network == "homestead";
		return "https://" + (useMainNet ? "api" : network) + ".etherscan.io/api?module=contract&action=getabi&address=" + address;
	}

	public static String readUtf8StreamToString(InputStream is) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		int len;
		while ((len = is.read(buf)) != -1) {
			bos.write(buf, 0, len);
		}
		bos.flush();
		return new String(bos.toByteArray(), StandardCharsets.UTF_8);
	}

	protected JsonArray getEthereumAbi(String network, String address) throws IOException {
		String url = getEtherscanAbiQueryUrl(network, address);
		URL etherscanApi = new URL(url);
		URLConnection conn = etherscanApi.openConnection();
		String json = readUtf8StreamToString(conn.getInputStream());
		JsonParser parser = new JsonParser();
		JsonObject response = parser.parse(json).getAsJsonObject();
		if (response.get("status").getAsString().equals("0")) {
			log.info("No ABI found for address " + address + " on Etherscan for network " + network);
			return null;
		} else {
			log.info("ABI found for address " + address + " on Etherscan for network " + network);
			return parser.parse(response.get("result").getAsString()).getAsJsonArray();
		}
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions = EthereumModuleOptions.readFrom(options);

		String address = addressParam.getValue();
		String abiString = MapTraversal.getString(config, "params[1].value");

		if (address.length() > 2 && !Numeric.toBigInt(address).equals(BigInteger.ZERO)) {
			if (abiString == null || abiString.trim().equals("") || abiString.trim().equals("[]")) {
				try {
					JsonArray etherscanAbi = getEthereumAbi(ethereumOptions.getNetwork(), address);
					abi = new EthereumABI(etherscanAbi);
					if (etherscanAbi != null) {
						// TODO: how do we make the retrieved ABI appear in ABI field on UI? Check if this already works
						MapTraversal.getMap(config, "params[1]").put("value", etherscanAbi.toString());
					}
				} catch (IOException e) {
					throw new RuntimeException("Error connecting to Etherscan", e);
				}
			} else {
				abi = new EthereumABI(abiString);
			}

			addOutput(out);

			EthereumOptions opts = ethereumOptions.getEthereumOptions();
			contract = new EthereumContract(abi, address, opts);
		} else {
			abi = new EthereumABI(abiString);
			contract = null;
		}

		// parsing failed, ABI is empty or invalid, or etherscan didn't return anything
		if (abi == null || abi.getFunctions().size() < 1) {
			abi = new EthereumABI("[{\"type\":\"fallback\",\"payable\":true}]");
		}
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions.writeTo(options);

		if (contract != null) {
			config.put("contract", contract.toMap());
		}

		return config;
	}

	/**
	 * GetContractAt may be activated during run-time when the address is updated.
	 * The resulting contract should point at the updated address.
	 * Changing ABI during runtime to something invalid will break this updating mechanism.
	 */
	@Override
	public void sendOutput() {
		String abiString = abiParam.getValue();
		String address = addressParam.getValue();

		try {
			EthereumABI abi = new EthereumABI(abiString);
			if (contract != null && !address.equals(contract.getAddress())) {
				contract = new EthereumContract(abi, address, ethereumOptions.getEthereumOptions());
			}
		} catch (JsonSyntaxException | IllegalStateException e) {
			log.error("Failed to parse ABI string: " + abiString, e);
		}

		if (contract != null) {
			out.send(contract);
		}
	}

	@Override
	public void clearState() {

	}
}
