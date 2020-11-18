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
import org.web3j.crypto.Credentials

class DevelopmentController {

	private static final String DU_NAME = 'du20201118a'

	// random users:
	Credentials adminCreds = Credentials.create('0x7aa27733cfc64a44ae605bfd6ec4a2c73579d37a661ddb72fcbdb4f4a752f30d')
	Credentials memberCreds = Credentials.create('0x31b5895362c37e7066b739bee7bcb3f327bbceb87d3a785a49217a8a539b985b')

	StreamrClientService streamrClientService
	ProductService productService

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def deploy() {
		println('deploy ' + DU_NAME)
		String nodeAddress = '0xFCAd0B19bB29D4674531d6f115237E16AfCE377c'  // the adress of private key 0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF
		DataUnion du = getClient().deployNewDataUnion(DU_NAME, adminCreds.getAddress(), 0.1d, [nodeAddress])
		long startTime = System.currentTimeMillis()
		boolean success = du.waitForDeployment(10 * 1000, 10 * 60 * 1000)
		println('took ' + (System.currentTimeMillis() - startTime) + ' ms')
		println('success: ' + success)
		println('address: ' + du.mainnetAddress())
		return render([success: success] as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def isMemberActive() {
		println('isMemberActive')
		DataUnion du = getClient().dataUnionFromName(DU_NAME)
		return render([isMemberActive: du.isMemberActive(memberCreds.getAddress())] as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def join() {
		println('join')
		DataUnion du = getClient().dataUnionFromName(DU_NAME)
		EthereumTransactionReceipt tr = du.joinMembers(memberCreds.getAddress())
		println("transaction: " + tr.getTransactionHash())
		return render([] as JSON)
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
