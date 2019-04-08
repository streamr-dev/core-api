package com.unifina.signalpath.blockchain

import org.web3j.abi.datatypes.Uint
import org.web3j.abi.datatypes.generated.Uint32

class Web3jHelperSpec {
	void "instantiateType produces correct Solidity type"() {
		when:

		then:
		Web3jHelper.instantiateType("uint",123) instanceof Uint
		Web3jHelper.instantiateType("uint",123) instanceof Uint32
	}
}
