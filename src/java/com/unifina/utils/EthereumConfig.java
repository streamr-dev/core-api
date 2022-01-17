package com.unifina.utils;

import org.apache.log4j.Logger;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
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

	/**
	 * @param method the connection method
	 * @return a Web3j connector, or null if it can't create
	 */
	public Web3j getWeb3j(RpcConnectionMethod method) {
		String url;
		Web3jService service;
		switch (method) {
			case HTTP:
				url = getRpcUrl();
				service = new HttpService(url);
				break;
			case WS:
				url = getWebsocketRpcUri();
				service = new WebSocketService(url, true);
				break;
			default:
				return null;
		}
		log.info("Creating RPC connection using method: " + method);
		return Web3j.build(service);
	}
}
