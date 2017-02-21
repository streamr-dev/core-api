package com.unifina.signalpath.blockchain;

import com.amazonaws.util.json.JSONObject;
import com.google.gson.JsonArray;
import com.unifina.utils.MapTraversal;
import grails.converters.JSON;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EthereumContract implements Serializable {

	private String address;
	private EthereumABI abi;

	/**
	 * Constructor for deployed contracts that already have an address.
	 * isDeployed() will return true.
     */
	public EthereumContract(String address, EthereumABI abi) {
		this(abi);
		this.address = address;
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

	public EthereumABI getABI() {
		return abi;
	}

	public boolean isDeployed() {
		return address != null;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new LinkedHashMap<>();
		if (address != null) {
			map.put("address", address);
		}
		map.put("abi", getABI().toList());
		return map;
	}

	public static EthereumContract fromMap(Map<String, Object> map) {
		List abiList = null;
		try {
			abiList = (List) map.get("abi");
		} catch (Exception e) {
		}
		return new EthereumContract(MapTraversal.getString(map, "address"), new EthereumABI(abiList));
	}
}
