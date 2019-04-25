package com.unifina.signalpath.blockchain;

import com.unifina.signalpath.ModuleOption;
import com.unifina.signalpath.ModuleOptions;
import com.unifina.utils.MapTraversal;
import grails.util.Holders;

import java.io.Serializable;
import java.util.Map;

public class EthereumModuleOptions implements Serializable {
	private String network = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.defaultNetwork");
	private double gasPriceWei = 20e9; // 20 Gwei

	void writeTo(ModuleOptions options) {
		writeNetworkOption(options);
		writeGasPriceOption(options);
	}

	static EthereumModuleOptions readFrom(ModuleOptions options) {
		EthereumModuleOptions ethOpts = new EthereumModuleOptions();
		ethOpts.readNetworkOption(options);
		ethOpts.readGasPriceOption(options);
		return ethOpts;
	}


	public String getNetwork() {
		return network;
	}

	public double getGasPriceWei() {
		return gasPriceWei;
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
			getServer(); // Throws if the network not valid
		}
	}

	public String getServer() {
		String url = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.networks." + network);
		if (url == null) {
			throw new RuntimeException("No url found for Ethereum bridge to network " + network);
		}
		return url;
	}

	public String getRpcUrl() {
		String url = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.rpcUrls." + network);
		if (url == null) {
			throw new RuntimeException("No rpcUrl found for Ethereum bridge to network " + network);
		}
		return url;
	}
}
