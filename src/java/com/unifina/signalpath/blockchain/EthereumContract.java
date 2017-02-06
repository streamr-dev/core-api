package com.unifina.signalpath.blockchain;

import com.unifina.utils.MapTraversal;

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
		this.abi = abi;
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
		map.put("address", getAddress());
		map.put("abi", getABI().toList());
		return map;
	}

	public static EthereumContract fromMap(Map<String, Object> map) {
		return new EthereumContract(MapTraversal.getString(map, "address"), new EthereumABI(MapTraversal.getList(map, "abi")));
	}
}
