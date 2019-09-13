package com.unifina.signalpath.blockchain;

import com.google.gson.JsonArray;
import com.unifina.utils.MapTraversal;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EthereumContract implements Serializable {

	public static final String KEY_ABI = "abi";
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_NETWORK = "network";

	private String address;
	private EthereumABI abi;
	private String network;

	/**
	 * Constructor for deployed contracts that already have an address.
	 * isDeployed() will return true.
     */
	public EthereumContract(String address, EthereumABI abi, String network) {
		this(abi);
		this.address = address;
		this.network = network;
	}

	/**
	 * Constructor for non-deployed contracts. Address will be null and
	 * isDeployed() will return false.
     */
	public EthereumContract(EthereumABI abi) {
		this.abi = abi != null ? abi : new EthereumABI((JsonArray) null);
	}

	public String getAddress() {
		return address;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public EthereumABI getABI() {
		return abi;
	}

	public boolean isDeployed() {
		return address != null;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new LinkedHashMap<>();
		if (address != null) {
			map.put(KEY_ADDRESS, address);
		}
		if (network != null) {
			map.put(KEY_NETWORK, network);
		}
		map.put(KEY_ABI, getABI().toList());
		return map;
	}

	public static EthereumContract fromMap(Map<String, Object> map) {
		List abiList = (List) map.get(KEY_ABI);
		String address = (String) map.get(KEY_ADDRESS);
		String network = (String) map.get(KEY_NETWORK);
		return new EthereumContract(address, new EthereumABI(abiList), network);
	}
}
