package com.unifina.signalpath.blockchain;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.utils.MapTraversal;

import java.util.Map;

public class EthereumContractInput extends Input<EthereumContract> {
	public EthereumContractInput(AbstractSignalPathModule owner, String name) {
		super(owner, name, "EthereumContract");
		setDrivingInput(false);
		setCanToggleDrivingInput(false);
		setJsClass("EthereumContractInput");
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		if (this.hasValue()) {
			config.put("value", this.getValue().toMap());
		}
		return config;
	}

	@Override
	public void setConfiguration(Map<String, Object> config) {
		super.setConfiguration(config);

		// Can get its value via config to support setting it in front end
		if (config.containsKey("value")) {
			receive(EthereumContract.fromMap(MapTraversal.getMap(config, "value")));
		}
	}
}
