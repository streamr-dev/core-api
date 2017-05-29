package com.unifina.signalpath.blockchain.templates;

import com.unifina.signalpath.ModuleOption;
import com.unifina.signalpath.ModuleOptions;
import com.unifina.utils.MapTraversal;
import grails.util.Holders;

import java.io.Serializable;
import java.util.Map;

public class EthereumModuleOptions implements Serializable {

	private String network;
	private String address;
	private String privateKey;

	public EthereumModuleOptions() {
		// default values
		network = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.defaultNetwork");
		address = "0x0";
		privateKey = "0x0";
	}

	public void writeTo(ModuleOptions options) {
		writeNetworkOption(options);
		options.add(new ModuleOption("address", address, "string"));
		options.add(new ModuleOption("privateKey", privateKey, "string"));
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

		if (options.getOption("address") != null) {
			ethOpts.setAddress(options.getOption("address").getString());
		}
		if (options.getOption("privateKey") != null) {
			ethOpts.setPrivateKey(options.getOption("privateKey").getString());
		}

		return ethOpts;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
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
