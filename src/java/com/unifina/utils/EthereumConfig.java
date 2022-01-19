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
		String key = "streamr.ethereum.networks." + network;
		String url = ApplicationConfig.getString(key);
		log.debug(String.format("Ethereum RPC HTTP URL key='%s' value='%s'", key, url));
		if (url == null) {
			String s = String.format("No RPC HTTP URL found for Ethereum network: '%s'", network);
			throw new RuntimeException(s);
		}
		return url;
	}

	public String getWebsocketRpcUri() {
		String key = "streamr.ethereum.wss." + network;
		String url = ApplicationConfig.getString(key);
		log.debug(String.format("Ethereum RPC WS URL key='%s' value='%s'", key, url));
		if (url == null) {
			String s = String.format("No RPC WS URL found for Ethereum network: '%s'", network);
			throw new RuntimeException(s);
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
		log.debug(String.format("Creating Ethereum RPC connection using method: %s", method));
		return Web3j.build(service);
	}
}
