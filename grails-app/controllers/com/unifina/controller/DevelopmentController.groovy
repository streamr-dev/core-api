package com.unifina.controller

import com.streamr.client.StreamrClient
import com.streamr.client.dataunion.DataUnion
import com.streamr.client.dataunion.DataUnionClient
import com.streamr.client.dataunion.contracts.DataUnionMainnet
import com.streamr.client.dataunion.contracts.DataUnionSidechain
import com.streamr.client.dataunion.EthereumTransactionReceipt
import com.unifina.domain.Product
import com.unifina.service.ProductService
import com.unifina.service.StreamrClientService
import com.unifina.utils.MapTraversal
import grails.converters.JSON
import grails.util.Holders

class DevelopmentController {

	private static final String DU_NAME = 'du20201118c'
	private static final String NODE_ADDRESS = '0xFCAd0B19bB29D4674531d6f115237E16AfCE377c'  // the adress of private key 0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF

	// random users:
	private static final String ADMIN_ADDRESS = '0x3AB95c1f65a732EC4370876881DAEd825A8e6F1b'  // private key: 0x7aa27733cfc64a44ae605bfd6ec4a2c73579d37a661ddb72fcbdb4f4a752f30d
	private static final String MEMBER_ADDRESS = '0x753182ff9378e7a8Cf243aDB69d70Cb8317D6B81'  // private key: 0x31b5895362c37e7066b739bee7bcb3f327bbceb87d3a785a49217a8a539b985b

	StreamrClientService streamrClientService
	ProductService productService

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def deploy(String name) {
		println('Deploy: ' + name)
		DataUnion du = getClient().deployNewDataUnion(name, ADMIN_ADDRESS, 0.1d, [NODE_ADDRESS])
		boolean success = du.waitForDeployment(10 * 1000, 10 * 60 * 1000)
		String address = du.mainnetAddress()
		println('Completed: success=' + success + ", address=" + address)
		return render([success: success, address: address] as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def isMemberActive(String name) {
		println('Check member activity: ' + name)
		DataUnion du = getClient().dataUnionFromName(name)
		return render([isActive: du.isMemberActive(MEMBER_ADDRESS)] as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def join(String name) {
		println('Join: ' + name)
		DataUnion du = getClient().dataUnionFromName(name)
		EthereumTransactionReceipt tr = du.joinMembers(MEMBER_ADDRESS)
		String transaction = tr.getTransactionHash()
		println("Transaction: " + transaction)
		return render([transaction: transaction] as JSON)
	}

	def getClient() {
		String privateKey = getEngineNodePrivateKey()
		return streamrClientService.getInstanceForThisEngineNode().dataUnionClient(privateKey, privateKey)
	}

	def getEngineNodePrivateKey() {
		return MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.nodePrivateKey")
	}

	def getDataUnionVersion(String contractAddress) {
		Product product = productService.findByBeneficiaryAddress(contractAddress)
		return product.dataUnionVersion
	}
}
