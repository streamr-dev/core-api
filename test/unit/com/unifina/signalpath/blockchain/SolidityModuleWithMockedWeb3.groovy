package com.unifina.signalpath.blockchain

class SolidityModuleWithMockedWeb3 extends SolidityModule {

	def deployArgs
	def sentWei
	def contractAsMap

	SolidityModuleWithMockedWeb3(Map contractAsMap) {
		this.contractAsMap = contractAsMap
	}

	// Stub out the web3 interface
	@Override
	protected StreamrWeb3Interface createWeb3Interface() {
		return new StreamrWeb3Interface("server", 4000000000) {
			@Override
			EthereumContract compile(String code) throws Exception {
				return EthereumContract.fromMap(contractAsMap)
			}

			@Override
			EthereumContract deploy(String code, List<Object> args, String sendWei, String address, String privateKey) throws Exception {
				deployArgs = args
				sentWei = sendWei
				def contract = EthereumContract.fromMap(contractAsMap)
				contract.address = "0x60f78aa68266c87fecec6dcb27672455111bb347"
				return contract
			}
		}
	}
}