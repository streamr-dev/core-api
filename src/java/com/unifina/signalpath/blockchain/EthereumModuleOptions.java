package com.unifina.signalpath.blockchain;

import com.unifina.signalpath.ModuleOption;
import com.unifina.signalpath.ModuleOptions;
import com.unifina.utils.MapTraversal;
import grails.util.Holders;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Map;

public class EthereumModuleOptions implements Serializable {
	private static final Logger log = Logger.getLogger(EthereumModuleOptions.class);

	private String network = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.defaultNetwork");
	private double gasPriceWei = 20e9; // 20 Gwei
	private int gasLimit = 6000000;

	void writeTo(ModuleOptions options) {
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

	public double getGasPriceWei() {
		return gasPriceWei;
	}
	public long getGasLimit() {
		return gasLimit;
	}

	private void writeGasPriceOption(ModuleOptions options) {
		options.add(ModuleOption.createDouble("gasPriceGWei", gasPriceWei / 1e9));
	}

	private void readGasPriceOption(ModuleOptions options) {
		ModuleOption gasPriceGWeiOption = options.getOption("gasPriceGWei");
		if (gasPriceGWeiOption != null) {
			gasPriceWei = gasPriceGWeiOption.getDouble() * 1e9;
		}
	}

	private void writeGasLimitOption(ModuleOptions options) {
		options.add(ModuleOption.createInt("gasLimit", gasLimit));
	}

	private void readGasLimitOption(ModuleOptions options) {
		ModuleOption gasLimitOption = options.getOption("gasLimit");
		if (gasLimitOption != null) {
			gasLimit = gasLimitOption.getInt();
		}
	}

	void writeNetworkOption(ModuleOptions options) {
		ModuleOption networkOption = ModuleOption.createString("network", network);

		// Add all configured networks
		Map<String, Object> networks = MapTraversal.getMap(Holders.getConfig(), "streamr.ethereum.networks");
		for (String network : networks.keySet()) {
			networkOption.addPossibleValue(network, network);
		}

		options.add(networkOption);
	}

	private void readNetworkOption(ModuleOptions options) {
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
}
