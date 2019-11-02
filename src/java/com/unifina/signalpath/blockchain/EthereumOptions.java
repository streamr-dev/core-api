package com.unifina.signalpath.blockchain;

import com.unifina.utils.MapTraversal;
import grails.util.Holders;
import org.apache.log4j.Logger;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketService;

import java.util.LinkedHashMap;
import java.util.Map;

public class EthereumOptions {
	private static final Logger log = Logger.getLogger(EthereumOptions.class);

	public static final String KEY_NETWORK = "network";
	public static final String KEY_GAS_PRICE = "gasPrice";
	public static final String KEY_GAS_LIMIT = "gasLimit";

	// TODO: use BigIntegers instead of longs
	public static final String defaultNetwork = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.defaultNetwork");
	public static final long defaultGasPriceWei = 20000000000L; // 20 Gwei
	public static final long defaultGasLimit = 10000000; // 10M

	public String network = defaultNetwork;
	public long gasPriceWei = defaultGasPriceWei;
	public long gasLimit = defaultGasLimit;

	public Map<String, Object> toMap() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put(KEY_NETWORK, network);
		map.put(KEY_GAS_PRICE, gasPriceWei);
		map.put(KEY_GAS_LIMIT, gasLimit);
		return map;
	}

	public static EthereumOptions fromMap(Map<String, Object> map) {
		EthereumOptions opts = new EthereumOptions();
		opts.network = map.getOrDefault(KEY_NETWORK, defaultNetwork).toString();
		opts.gasPriceWei = Long.valueOf(map.getOrDefault(KEY_GAS_PRICE, defaultGasPriceWei).toString(), 10);
		opts.gasLimit = Long.valueOf(map.getOrDefault(KEY_GAS_LIMIT, defaultGasLimit).toString(), 10);
		return opts;
	}

	public String getRpcUrl() {
		String url = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.networks." + network);
		if (url == null) {
			throw new RuntimeException("No rpcUrl found for Ethereum network " + network);
		}
		return url;
	}

	public String getWebsocketRpcUri() {
		String url = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.wss." + network);
		if (url == null) {
			log.warn("No websockets URI found for Ethereum network " + network);
		}
		return url;
	}

	public Web3j getWeb3j() {
		return getWeb3j(EthereumModuleOptions.RpcConnectionMethod.HTTP);
	}

	public Web3j getWeb3j(EthereumModuleOptions.RpcConnectionMethod preferredMethod) {
		Web3j web3j;
		int start = preferredMethod.ordinal();
		int len = EthereumModuleOptions.RpcConnectionMethod.values().length;

		// cycle through all connection methods starting with preferredMethod
		for (int i = 0; i < len; i++) {
			EthereumModuleOptions.RpcConnectionMethod method = EthereumModuleOptions.RpcConnectionMethod.values()[start + i % len];
			web3j = getWeb3jUsingMethod(method);
			if (web3j != null) {
				log.info("Created RPC using connection method: " + method);
				return web3j;
			}
		}
		return null;
	}

	/**
	 *
	 * @param method the connection method
	 * @return a Web3j connector, or null if it can't create
	 */
	private Web3j getWeb3jUsingMethod(EthereumModuleOptions.RpcConnectionMethod method){
		String url;
		switch(method){
			case HTTP:
				if ((url = getRpcUrl()) == null) {
					log.warn("No http RPC URL specified");
					return null;
				}
				return Web3j.build(new HttpService(url));
			case WS:
				if ((url = getWebsocketRpcUri()) == null) {
					log.warn("No ws RPC URL specified");
					return null;
				}
				return Web3j.build(new WebSocketService(url, true));
			default:
				return null;
		}
	}
}
