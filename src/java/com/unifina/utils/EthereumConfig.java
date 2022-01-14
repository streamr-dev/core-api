package com.unifina.utils;

import org.apache.log4j.Logger;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketService;

public final class EthereumConfig {
	private static final Logger log = Logger.getLogger(EthereumConfig.class);

	private final String network;

	public enum RpcConnectionMethod {
		WS,
		HTTP
	}

	public EthereumConfig(final String network) {
		this.network = network;
	}

	public String getRpcUrl() {
		String url = ApplicationConfig.getString("streamr.ethereum.networks." + network);
		if (url == null) {
			throw new RuntimeException("No rpcUrl found for Ethereum network " + network);
		}
		return url;
	}

	public String getWebsocketRpcUri() {
		String url = ApplicationConfig.getString("streamr.ethereum.wss." + network);
		if (url == null) {
			throw new RuntimeException("No websockets URI found for Ethereum network " + network);
		}
		return url;
	}

	public Web3j getWeb3j(RpcConnectionMethod preferredMethod) {
		int start = preferredMethod.ordinal();
		int len = RpcConnectionMethod.values().length;

		// cycle through all connection methods starting with preferredMethod
		for (int i = 0; i < len; i++) {
			RpcConnectionMethod method = RpcConnectionMethod.values()[start + i % len];
			Web3j web3j = getWeb3jUsingMethod(method);
			if (web3j != null) {
				log.info("Created RPC using connection method: " + method);
				return web3j;
			}
		}
		return null;
	}

	/**
	 * @param method the connection method
	 * @return a Web3j connector, or null if it can't create
	 */
	private Web3j getWeb3jUsingMethod(RpcConnectionMethod method) {
		String url;
		switch (method) {
			case HTTP:
				url = getRpcUrl();
				return Web3j.build(new HttpService(url));
			case WS:
				url = getWebsocketRpcUri();
				return Web3j.build(new WebSocketService(url, true));
			default:
				return null;
		}
	}
}
