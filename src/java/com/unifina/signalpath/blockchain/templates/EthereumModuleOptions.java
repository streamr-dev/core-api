package com.unifina.signalpath.blockchain.templates;

import com.unifina.signalpath.ModuleOption;
import com.unifina.signalpath.ModuleOptions;
import com.unifina.utils.MapTraversal;
import grails.util.Holders;

import java.io.Serializable;
import java.util.Map;

public class EthereumModuleOptions implements Serializable {

	private String network;

	public EthereumModuleOptions() {
		// default values
		network = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.defaultNetwork");
	}

	public void writeTo(ModuleOptions options) {
		writeNetworkOption(options);
	}

	public void writeNetworkOption(ModuleOptions options) {
		ModuleOption networkOption = new ModuleOption("network", network, ModuleOption.OPTION_STRING);

		// Add all configured networks
		Map<String, Object> networks = MapTraversal.getMap(Holders.getConfig(), "streamr.ethereum.networks");
		for (String network : networks.keySet()) {
			networkOption.addPossibleValue(network, network);
		}

		options.add(networkOption);
	}

	public void readNetworkOption(ModuleOptions options) {
		if (options.getOption("network") != null) {
			setNetwork(options.getOption("network").getString());

			// Throws if the network was not valid
			getServer();
		}
	}

	public static EthereumModuleOptions readFrom(ModuleOptions options) {
		EthereumModuleOptions ethOpts = new EthereumModuleOptions();
		ethOpts.readNetworkOption(options);
		return ethOpts;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getServer() {
		String url = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.networks."+network);

		if (url == null) {
			throw new RuntimeException("No url found for Ethereum bridge to network "+network);
		} else {
			return url;
		}
	}
}
