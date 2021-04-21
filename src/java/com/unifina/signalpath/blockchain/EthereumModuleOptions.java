package com.unifina.signalpath.blockchain;

import com.unifina.utils.ApplicationConfig;
import org.apache.log4j.Logger;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketService;

import java.io.Serializable;

public class EthereumModuleOptions implements Serializable {
	private static final Logger log = Logger.getLogger(EthereumModuleOptions.class);

	private String network = ApplicationConfig.getString("streamr.ethereum.defaultNetwork");
	private double gasPriceWei = 20e9; // 20 Gwei
	private int gasLimit = 6000000;

	public enum RpcConnectionMethod {
		WS,
		HTTP
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public double getGasPriceWei() {
		return gasPriceWei;
	}

	public long getGasLimit() {
		return gasLimit;
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
			log.warn("No websockets URI found for Ethereum network " + network);
		}
		return url;
	}

	public Web3j getWeb3j() {
		return getWeb3j(RpcConnectionMethod.HTTP);
	}

	public Web3j getWeb3j(RpcConnectionMethod preferredMethod) {
		Web3j web3j;
		int start = preferredMethod.ordinal();
		int len = RpcConnectionMethod.values().length;

		// cycle through all connection methods starting with preferredMethod
		for (int i = 0; i < len; i++) {
			RpcConnectionMethod method = RpcConnectionMethod.values()[start + i % len];
			web3j = getWeb3jUsingMethod(method);
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
