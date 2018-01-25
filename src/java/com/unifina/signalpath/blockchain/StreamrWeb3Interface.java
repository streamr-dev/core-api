package com.unifina.signalpath.blockchain;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.Unirest;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Grails side of the streamr-web3 node.js Ethereum bridge */
public class StreamrWeb3Interface implements Serializable {

	private static final Logger log = Logger.getLogger(StreamrWeb3Interface.class);
	private final String server;
	private final Double gasPriceWei;

	public StreamrWeb3Interface(String server, Double gasPriceWei) {
		this.server = server;
		this.gasPriceWei = gasPriceWei;
	}

	/** @returns EthereumContract with isDeployed() false */
	public EthereumContract compile(String code) throws Exception {
		String bodyJson = new Gson().toJson(Collections.singletonMap("code", code));

		log.info("compile request: "+bodyJson);

		String responseJson = Unirest.post(server + "/compile")
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
			// If several contracts were returned, pick the one with longest bytecode; that's most probably the "main" contract
			ContractMetadata mainContract = returned.contracts.get(0);
			for (int i = 1; i < returned.contracts.size(); i++) {
				ContractMetadata c = returned.contracts.get(i);
				if (c.bytecode.length() > mainContract.bytecode.length()) {
					mainContract = c;
				}
			}
			// TODO: bring returned.errors to UI somehow? They're warnings probably since compilation was successful
			return new EthereumContract(mainContract.address, new EthereumABI(mainContract.abi));
		} else {
			// TODO java 8: String.join
			throw new RuntimeException(new Gson().toJson(returned.errors));
		}
	}

	/**
	 * @param sendWei String representation of decimal value of wei to send
	 * @returns EthereumContract that isDeployed()
	 **/
	public EthereumContract deploy(String code, List<Object> args, String sendWei, String address, String privateKey) throws Exception {
		Map<String, Object> body = new HashMap<>();
		body.put("source", address);
		body.put("key", privateKey);
		body.put("gasprice", gasPriceWei);
		body.put("code", code);
		body.put("args", args);
		body.put("value", sendWei);
		String bodyJson = new Gson().toJson(body);

		// TODO: won't this affect globally? Shouldn't probably have static method invocation here.
		Unirest.setTimeouts(10*1000, 10*60*1000); // wait patiently for the next mined block, up to 10 minutes

		log.info("deploy request: "+bodyJson);

		String responseJson = Unirest.post(server + "/deploy")
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

	public String getServer() {
		return server;
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
}