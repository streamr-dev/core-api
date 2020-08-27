package com.unifina.service

import com.unifina.utils.MapTraversal
import grails.transaction.Transactional
import grails.util.Holders
import org.apache.log4j.Logger
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService

import javax.annotation.PostConstruct

enum RPCConnectionMethod {
	WS,
	HTTP
}

class EthereumConfig {
	private String network = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.defaultNetwork")
	private double gasPriceWei = 20e9 // 20 Gwei
	private int gasLimit = 6000000

	public String getRpcUrl() {
		String url = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.networks." + network)
		if (url == null) {
			throw new RuntimeException("No RPC URL found for Ethereum network " + network)
		}
		return url
	}

	public String getWebsocketRpcUri() {
		String url = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.wss." + network)
		if (url == null) {
			throw new IllegalArgumentException("No WebSockets URI found for Ethereum network " + network)
		}
		return url
	}
}

@Transactional(readOnly = true)
class EthereumNameService {
	private static final Logger log = Logger.getLogger(EthereumNameService)

	private Web3jHelperService web3jHelperService
	private EthereumConfig config
	private Web3j web3j

	@PostConstruct
	void init() {
		this.config = new EthereumConfig()
		final HttpService httpService = new HttpService(this.config.getRpcUrl())
		this.web3j = Web3j.build(httpService)
	}

	void resolveOrSomething() {
    }
}
