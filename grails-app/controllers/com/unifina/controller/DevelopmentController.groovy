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
import grails.compiler.GrailsCompileStatic

class DevelopmentController {

	// random users:
	Credentials adminCreds = Credentials.create('0x7aa27733cfc64a44ae605bfd6ec4a2c73579d37a661ddb72fcbdb4f4a752f30d')
	Credentials memberCreds = Credentials.create('0x172b2ac1381ec11e7e61f98756621220b65db49ab4663910483960fc803a6465')

	String DU_NAME = 'testdu.20201109d'

	StreamrClientService streamrClientService
	ProductService productService

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def deploy() {
		println('deploy ' + DU_NAME)
		Credentials agentCreds = getEngineNodeCredentials()
		DataUnion du = getClient().deployNewDataUnion(DU_NAME, adminCreds.getAddress(), new BigInteger("1234"), [agentCreds.getAddress()])
		println(du)
		boolean success = du.waitForDeployment(10000L, 600000L)
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
		println("transaction: " + tr.txHash())
		return render([] as JSON)
	}

	def getClient() {
		Credentials agentCreds = getEngineNodeCredentials()
		return streamrClientService.getInstanceForThisEngineNode().dataUnionClient(agentCreds, agentCreds)
	}

	def getEngineNodeCredentials() {
		String nodePrivateKey = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.nodePrivateKey")
		return Credentials.create(nodePrivateKey)
	}

	def getDataUnionVersion(String contractAddress) {
		Product product = productService.findByBeneficiaryAddress(contractAddress)
		return product.dataUnionVersion
	}
}
