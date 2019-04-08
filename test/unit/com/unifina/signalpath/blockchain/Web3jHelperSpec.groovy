package com.unifina.signalpath.blockchain

import com.unifina.ModuleTestingSpecification
import com.unifina.security.StreamrApi
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Uint
import org.web3j.abi.datatypes.generated.Uint32

class Web3jHelperSpec extends ModuleTestingSpecification{
	@StreamrApi
	void "instantiateType produces correct Solidity type"() {
		when:

		expect:
		Web3jHelper.instantiateType("uint",123) instanceof Uint
		Web3jHelper.instantiateType("address","0x123") instanceof Address
	}
}
