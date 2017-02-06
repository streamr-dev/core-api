package com.unifina.signalpath.blockchain;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Output;

public class EthereumContractOutput extends Output<EthereumContract> {

	public EthereumContractOutput(AbstractSignalPathModule owner, String name) {
		super(owner, name, "EthereumContract");
	}
}
