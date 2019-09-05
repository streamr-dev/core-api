package com.unifina.signalpath.blockchain;

import com.google.gson.*;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.ModuleOptions;
import com.unifina.signalpath.StringParameter;
import com.unifina.utils.MapTraversal;
import grails.util.Holders;
import org.apache.log4j.Logger;
import org.web3j.utils.Numeric;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Get Ethereum contract at given address
 */
public class GetEthereumContractAt extends AbstractSignalPathModule {
	private static final Logger log = Logger.getLogger(GetEthereumContractAt.class);

	protected final String ETHERSCAN_IO = "etherscan.io";
	protected final String ETHERSCAN_IO_QUERY = "/api?module=contract&action=getabi&address=";
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

	protected String getApiUrl(String network, String address) {
		StringBuilder sb = new StringBuilder();
		sb.append("https://");
		if (network == null || network.startsWith("main")) {
			sb.append("api");
		} else {
			sb.append(network);
		}
		sb.append("." + ETHERSCAN_IO + ETHERSCAN_IO_QUERY + address);
		return sb.toString();
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
		String url = getApiUrl(network, address);
		URL etherscanapi = new URL(url);
		URLConnection conn = etherscanapi.openConnection();
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
		String address = addressParam.getValue();
		String abiString = MapTraversal.getString(config, "params[1].value");
		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions = EthereumModuleOptions.readFrom(options);
		String network = ethereumOptions.getNetwork();
		if(contract != null){
			contract.setNetwork(network);
		}
		//address != 0x0
		if (address.length() > 2 && !Numeric.toBigInt(address).equals(BigInteger.ZERO)) {
			if (abiString == null || abiString.trim().equals("") || abiString.trim().equals("[]")) {
				try {
					//try to pull from etherescan
					JsonArray etherscan_abi = getEthereumAbi(network, address);
					abi = new EthereumABI(etherscan_abi);
					if (etherscan_abi != null) {
						//how do we make the retrieved ABI appear in ABI field on UI?
						MapTraversal.getMap(config, "params[1]").put("value", etherscan_abi.toString());
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				abi = new EthereumABI(abiString);
			}

			addOutput(out);
			contract = new EthereumContract(address, abi, ethereumOptions.getNetwork());
		}
		//address = 0x0
		else {
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
			contract = new EthereumContract(addressParam.getValue(), abi, ethereumOptions.getNetwork());
		}

		if (contract != null) {
			out.send(contract);
		}
	}

	@Override
	public void clearState() {

	}
}
