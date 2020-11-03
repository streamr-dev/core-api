package com.unifina.controller

import com.streamr.client.StreamrClient
import com.streamr.client.dataunion.DataUnionClient
import com.streamr.client.dataunion.contracts.DataUnionMainnet
import com.streamr.client.dataunion.contracts.DataUnionSidechain
import com.unifina.domain.Product
import com.unifina.service.ProductService
import com.unifina.service.StreamrClientService
import com.unifina.utils.MapTraversal
import grails.converters.JSON
import grails.util.Holders
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.protocol.core.methods.response.TransactionReceipt

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
		String nodePrivateKey = '0x5e98cce00cff5dea6b454889f359a4ec06b9fa6b88e9d69b86de8e1c81887da0' // MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.nodePrivateKey")
		Credentials creds = Credentials.create(nodePrivateKey)
		StreamrClient client = streamrClientService.getInstanceForThisEngineNode()
		DataUnionClient duClient = client.dataUnionClient(creds, creds)   // both chains have the same in dev and prod environment
		Address deployer = new Address(creds.getAddress())
		Utf8String duname = new Utf8String("TEST DU-" + System.currentTimeMillis())
		Address duAddress = duClient.factoryMainnet().mainnetAddress(deployer, duname).send()
		Address sidechainAddress = duClient.factorySidechain().sidechainAddress(duAddress).send()
		DataUnionSidechain duSidechain = duClient.sidechainDU(sidechainAddress.getValue())
		DataUnionMainnet duMainnet = duClient.mainnetDU(duAddress.getValue())
		TransactionReceipt tr = duClient.factoryMainnet().deployNewDataUnion(
				deployer,
				new Uint256(0),
				new DynamicArray<Address>(deployer),
				duname
		).send()
		println(tr)
		duClient.waitForMainnetTx(tr.getTransactionHash(), 10000, 600000)
		println(tr.contractAddress)
		println(duMainnet.token().send().getValue())
		println(duClient.waitForSidechainContract(duSidechain.getContractAddress(), 10000, 600000))
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
