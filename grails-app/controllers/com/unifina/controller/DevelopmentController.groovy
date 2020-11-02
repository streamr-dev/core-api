package com.unifina.controller
import grails.converters.JSON
import com.streamr.client.*
import com.streamr.client.rest.*
import com.streamr.client.authentication.*
import com.streamr.client.dataunion.*
import com.streamr.client.dataunion.contracts.*
import com.streamr.client.options.*
import com.streamr.client.utils.*
import org.web3j.abi.datatypes.*
import org.web3j.abi.datatypes.generated.*
import com.unifina.service.StreamrClientService
import com.unifina.service.ProductService
import com.unifina.domain.Product
import org.web3j.crypto.Credentials
import org.web3j.protocol.core.methods.response.TransactionReceipt
import grails.util.Holders
import com.unifina.utils.MapTraversal

// 0xa3d1f7...df847de1 eli Account3 teki streamin ja productin
// 0xaab6c9774cb2a8e825961d5f40e445432d7589aa eli Account1 haluaa joinata
class DevelopmentController {

	StreamrClientService streamrClientService
	ProductService productService

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def foobar() {
		createDataUnion()
		return render([] as JSON)
		/*println("FOOBAR")
		StreamrClient client = streamrClientService.getInstanceForThisEngineNode()
		DataUnionClient duClient = client.dataUnionClient(getEngineNodeCredentials(), getEngineNodeCredentials())  // TODO käykö samat credentialit sekä mainnettiin että sidechainiin?
		DataUnionSidechain sidechain = duClient.sidechainDU('0xed56b560d42917185a829789c3ef1e118a421acc')
		boolean isActive = DataUnionClient.isMemberActive(sidechain, new Address('0xa3d1f77acff0060f7213d7bf3c7fec78df847de1'))
		println('IS ACTIVE = ' + isActive)
		return render([dataUnionVersion: getDataUnionVersion('0xed56b560d42917185a829789c3ef1e118a421acc')] as JSON)*/
	}

	def createDataUnion() {
		String nodePrivateKey = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.nodePrivateKey")
		Credentials creds = Credentials.create(nodePrivateKey)
		StreamrClient client = streamrClientService.getInstanceForThisEngineNode()
		DataUnionClient duClient = client.dataUnionClient(getEngineNodeCredentials(), getEngineNodeCredentials())
		Address deployer = new Address(creds.getAddress())
		TransactionReceipt tr = duClient.factoryMainnet().deployNewDataUnion(
				deployer,
				new Uint256(0),
				new DynamicArray<Address>(deployer),
				'TEST DU'
		).send()
		println(tr)
		println(tr.contractAddress)
		client.waitForMainnetTx(tr.getTransactionHash(), 10000, 600000)
		println(duMainnet.token().send().getValue())
		println(client.waitForSidechainContract(duSidechain.getContractAddress(), 10000, 600000))
	}

	def getDataUnionVersion(String contractAddress) {
		Product product = productService.findByBeneficiaryAddress(contractAddress)
		return product.dataUnionVersion
	}

	def getEngineNodeCredentials() {
		String nodePrivateKey = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.nodePrivateKey")
		println("NodePrivateKey:" + nodePrivateKey)
		return Credentials.create(nodePrivateKey)
	}
}