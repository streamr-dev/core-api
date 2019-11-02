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

	private String address;
	private EthereumABI abi;
	private EthereumOptions ethereumOptions;

	/**
	 * Constructor for deployed contracts that already have an address, so isDeployed() will return true.
     */
	public EthereumContract(EthereumABI abi, String address, EthereumOptions opts) {
		this(abi, opts);
		this.address = address;
	}

	/**
	 * Constructor for non-deployed contracts. Address will be null, so isDeployed() will return false.
     */
	public EthereumContract(EthereumABI abi, EthereumOptions opts) {
		this.abi = abi != null ? abi : new EthereumABI((JsonArray) null);
		this.ethereumOptions = opts;
	}

	public String getAddress() {
		return address;
	}

	public EthereumOptions getEthereumOptions() {
		return EthereumOptions.fromMap(ethereumOptions.toMap());
	}

	public void setEthereumOptions(EthereumOptions opts) {
		this.ethereumOptions = opts;
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
		if (ethereumOptions != null) {
			map.putAll(ethereumOptions.toMap());
		}
		map.put(KEY_ABI, getABI().toList());
		return map;
	}

	public static EthereumContract fromMap(Map<String, Object> map) {
		List abiList = (List) map.get(KEY_ABI);
		String address = (String) map.get(KEY_ADDRESS);
		EthereumOptions opts = EthereumOptions.fromMap(map);
		return new EthereumContract(new EthereumABI(abiList), address, opts);
	}
}
