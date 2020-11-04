package com.unifina.controller

import com.streamr.client.StreamrClient
import com.streamr.client.dataunion.DataUnion
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
import grails.compiler.GrailsCompileStatic

// 0xa3d1f7...df847de1 eli Account3 teki streamin ja productin
// 0xaab6c9774cb2a8e825961d5f40e445432d7589aa eli Account1 haluaa joinata

class DevelopmentController {

	// some test user that has ether balance: (https://github.com/streamr-dev/smart-contracts-init/blob/master/index.js)
	// TODO is balance needed if agent just add members and does not deploy the DU (which is the real use case here)
	Credentials agentCreds = Credentials.create('0x5e98cce00cff5dea6b454889f359a4ec06b9fa6b88e9d69b86de8e1c81887da0')
	// random user:
	Credentials adminCreds = Credentials.create('0x7aa27733cfc64a44ae605bfd6ec4a2c73579d37a661ddb72fcbdb4f4a752f30d')
	// random user:
	Credentials memberCreds = Credentials.create('0x172b2ac1381ec11e7e61f98756621220b65db49ab4663910483960fc803a6465')

	String DU_NAME = 'testdu.202011041237'

	StreamrClientService streamrClientService
	ProductService productService

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def deploy() {
		println('deploy ' + DU_NAME)
		DataUnion du = getClient().deployNewDataUnion(DU_NAME, adminCreds.getAddress(), new BigInteger("1234"), [agentCreds.getAddress()])
		println(du)
		boolean success = du.waitForDeployment(10000L, 600000L)
		println('success: ' + success)
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
		TransactionReceipt tr = du.joinMembers(memberCreds.getAddress())
		println(tr)
		return render([] as JSON)
	}

	def getClient() {
		return streamrClientService.getInstanceForThisEngineNode().dataUnionClient(agentCreds, agentCreds)
	}

	/*@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def foobar() {
		String nodePrivateKey = '0x5e98cce00cff5dea6b454889f359a4ec06b9fa6b88e9d69b86de8e1c81887da0' // MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.nodePrivateKey")
		Credentials creds = Credentials.create(nodePrivateKey)
		StreamrClient client = streamrClientService.getInstanceForThisEngineNode()
		DataUnionClient duClient = client.dataUnionClient(creds, creds)   // both chains have the same in dev and prod environment

		//createDataUnion()
		def memberAddress = '0x3AB95c1f65a732EC4370876881DAEd825A8e6F1b' // '0xaab6c9774cb2a8e825961d5f40e445432d7589aa'
		def duAddress = '0xd723ddbbb3c1fa8b0b6ed61e1f6fa68fe7147e39' // getDUAddress('TEST DU-1604410377278')
		isMemberActive(memberAddress, duAddress, duClient)
		//addMember(memberAddress, duAddress, duClient)
		return render([] as JSON)
	}

	def addMember(memberAddress, duAddress, duClient) {
		def duSidechain = getDUSidechain(duAddress, duClient)
		TransactionReceipt tr = duSidechain.addMember(new Address(memberAddress)).send()
		duClient.waitForSidechainTx(tr.getTransactionHash(), 10000, 600000)
		println('SIZE:' + duSidechain.activeMemberCount().send().getValue())
	}

	def isMemberActive(memberAddress, duAddress, duClient) {
		boolean isActive = DataUnionClient.isMemberActive(getDUSidechain(duAddress, duClient), new Address(memberAddress))
		println('IS ACTIVE: ' + isActive)
	}

	def getDUSidechain(duAddress, duClient) {
		Address sidechainAddress = duClient.factorySidechain().sidechainAddress(new Address(duAddress)).send()
		return duClient.sidechainDU(sidechainAddress.getValue())
	}

	def getDUAddress(duname) {
		String nodePrivateKey = '0x5e98cce00cff5dea6b454889f359a4ec06b9fa6b88e9d69b86de8e1c81887da0' // MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.nodePrivateKey")
		Credentials creds = Credentials.create(nodePrivateKey)
		Address deployer = new Address(creds.getAddress())
		StreamrClient client = streamrClientService.getInstanceForThisEngineNode()
		DataUnionClient duClient = client.dataUnionClient(creds, creds)   // both chains have the same in dev and prod environment
		Address duAddress = duClient.factoryMainnet().mainnetAddress(deployer, new Utf8String(duname)).send()
		println('DU ADDRESS = ' + duAddress)
	}

	def createDataUnion() {
		String nodePrivateKey = '0x5e98cce00cff5dea6b454889f359a4ec06b9fa6b88e9d69b86de8e1c81887da0' // MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.nodePrivateKey")
		Utf8String duname = new Utf8String("TEST DU-" + System.currentTimeMillis())
		Credentials creds = Credentials.create(nodePrivateKey)
		StreamrClient client = streamrClientService.getInstanceForThisEngineNode()
		DataUnionClient duClient = client.dataUnionClient(creds, creds)   // both chains have the same in dev and prod environment
		Address deployer = new Address(creds.getAddress())
		println('Deploying ' + duname)
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
	}*/

	/*def getEngineNodeCredentials() {
		String nodePrivateKey = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.nodePrivateKey")
		println("NodePrivateKey:" + nodePrivateKey)
		return Credentials.create(nodePrivateKey)
	}*/
}
