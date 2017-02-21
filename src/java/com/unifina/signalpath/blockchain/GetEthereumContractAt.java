package com.unifina.signalpath.blockchain;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.StringParameter;
import com.unifina.utils.MapTraversal;

import java.util.Map;

/**
 * Get Ethereum contract at given address
 */
public class GetEthereumContractAt extends AbstractSignalPathModule {
	private StringParameter addressParam = new StringParameter(this, "address", "0x");
	private StringParameter abiParam = new StringParameter(this, "ABI", "[]");
	private EthereumContractOutput out = new EthereumContractOutput(this, "contract");

	private EthereumContract contract;

	@Override
	public void init() {
		addInput(addressParam);
		addressParam.setUpdateOnChange(true);
		abiParam.setUpdateOnChange(true);
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		String address = addressParam.getValue();
		String abiJson = MapTraversal.getString(config, "params[1].value", "[]");

		// TODO: check address is valid?
		if (address.length() > 2) {
			// TODO: query backend for known ABI
			addInput(abiParam);
			addOutput(out);

			EthereumABI abi = new EthereumABI(abiJson);
			if (abi.getFunctions().size() < 1) {
				abi = new EthereumABI("[{\"type\":\"fallback\"}]");
			}

			contract = new EthereumContract(address, abi);
		} else {
			contract = null;
		}
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		if (contract != null) {
			config.put("contract", contract.toMap());
		}

		return config;
	}

	@Override
	public void sendOutput() {

	}

	@Override
	public void clearState() {

	}
}
