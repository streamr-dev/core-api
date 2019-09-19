package com.unifina.signalpath.blockchain;

import com.unifina.signalpath.ModuleOption;
import com.unifina.signalpath.ModuleOptions;
import com.unifina.utils.MapTraversal;
import grails.util.Holders;
import org.apache.log4j.Logger;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketService;

import java.io.Serializable;
import java.util.Map;

public class EthereumModuleOptions implements Serializable {
	private static final Logger log = Logger.getLogger(EthereumModuleOptions.class);

	private String network = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.defaultNetwork");
	private double gasPriceWei = 20e9; // 20 Gwei
	private int gasLimit = 6000000;

	public enum RpcConectionMethod {ws,http};

	public void writeTo(ModuleOptions options) {
		writeNetworkOption(options);
		writeGasPriceOption(options);
		writeGasLimitOption(options);
	}

	static EthereumModuleOptions readFrom(ModuleOptions options) {
		EthereumModuleOptions ethOpts = new EthereumModuleOptions();
		ethOpts.readNetworkOption(options);
		ethOpts.readGasPriceOption(options);
		ethOpts.readGasLimitOption(options);
		return ethOpts;
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

	public void writeGasPriceOption(ModuleOptions options) {
		options.add(ModuleOption.createDouble("gasPriceGWei", gasPriceWei / 1e9));
	}

	public void readGasPriceOption(ModuleOptions options) {
		ModuleOption gasPriceGWeiOption = options.getOption("gasPriceGWei");
		if (gasPriceGWeiOption != null) {
			gasPriceWei = gasPriceGWeiOption.getDouble() * 1e9;
		}
	}

	public void writeGasLimitOption(ModuleOptions options) {
		options.add(ModuleOption.createInt("gasLimit", gasLimit));
	}

	public void readGasLimitOption(ModuleOptions options) {
		ModuleOption gasLimitOption = options.getOption("gasLimit");
		if (gasLimitOption != null) {
			gasLimit = gasLimitOption.getInt();
		}
	}

	public void writeNetworkOption(ModuleOptions options) {
		ModuleOption networkOption = ModuleOption.createString("network", network);

		// Add all configured networks
		Map<String, Object> networks = MapTraversal.getMap(Holders.getConfig(), "streamr.ethereum.networks");
		for (String network : networks.keySet()) {
			networkOption.addPossibleValue(network, network);
		}

		options.add(networkOption);
	}

  public void readNetworkOption(ModuleOptions options) {
		ModuleOption networkOption = options.getOption("network");
		if (networkOption != null) {
			network = networkOption.getString();
			getRpcUrl(); // Throws if the network not valid
		}
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
		return getWeb3j(RpcConectionMethod.http);
	}

	public Web3j getWeb3j(RpcConectionMethod preferredMethod){
		Web3j web3j;
		int start = preferredMethod.ordinal();
		int len = RpcConectionMethod.values().length;
		//cycle thru all connection methods starting with preferredMethod
		for(int i=0;i<len;i++){
			RpcConectionMethod method  = RpcConectionMethod.values()[start + i % len];
			web3j = getWeb3jUsingMethod(method);
			if(web3j != null){
				log.info("Created RPC using connection method: "+method);
				return web3j;
			}
		}
		return null;
	}
	private Web3j getWeb3jUsingMethod(RpcConectionMethod preferredMethod){
		String url;
		switch(preferredMethod){
			case http:
				if((url = getRpcUrl()) == null){
					log.warn("No http RPC URL specified");
				}
				return Web3j.build(new HttpService(url));
			case ws :
				if((url = getWebsocketRpcUri()) == null){
					log.warn("No ws RPC URL specified");
				}
				return Web3j.build(new WebSocketService(url,true));
			default:
				return null;
		}
	}
}
