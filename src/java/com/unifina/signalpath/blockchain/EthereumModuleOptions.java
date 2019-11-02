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

	private EthereumOptions opts = new EthereumOptions();

	public enum RpcConnectionMethod {
		WS,
		HTTP
	};

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

	public EthereumOptions getEthereumOptions() {
		return EthereumOptions.fromMap(opts.toMap());
	}

	public long getGasPriceWei() {
		return opts.gasPriceWei;
	}

	public long getGasLimit() {
		return opts.gasLimit;
	}

	public String getNetwork() {
		return opts.network;
	}

	public void writeGasPriceOption(ModuleOptions options) {
		options.add(ModuleOption.createDouble("gasPriceGWei", opts.gasPriceWei / 1e9));
	}

	public void readGasPriceOption(ModuleOptions options) {
		ModuleOption gasPriceGWeiOption = options.getOption("gasPriceGWei");
		if (gasPriceGWeiOption != null) {
			opts.gasPriceWei = Math.round(gasPriceGWeiOption.getDouble() * 1e9);
		}
	}

	// TODO: we probably need a BigInteger in ModuleOptions that is transmitted as a String
	public void writeGasLimitOption(ModuleOptions options) {
		options.add(ModuleOption.createInt("gasLimit", (int) opts.gasLimit));
	}

	public void readGasLimitOption(ModuleOptions options) {
		ModuleOption gasLimitOption = options.getOption("gasLimit");
		if (gasLimitOption != null) {
			opts.gasLimit = gasLimitOption.getInt();
		}
	}

	public void writeNetworkOption(ModuleOptions options) {
		ModuleOption networkOption = ModuleOption.createString("network", opts.network);

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
			opts.network = networkOption.getString();
			opts.getRpcUrl(); // Throws if the network not valid
		}
	}
}
