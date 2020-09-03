package com.unifina.utils

import grails.util.Holders
import org.web3j.crypto.Credentials
import org.web3j.ens.Contracts
import org.web3j.ens.NameHash
import org.web3j.ens.contracts.generated.ENS
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.RemoteCall
import org.web3j.protocol.http.HttpService

class Web3jWrapper {
	String getENSDomainOwner(String domain) {
		//Web3j web3j = Web3jHelper.getWeb3jConnectionFromConfig()
		//Web3j web3j = Web3j.build(new HttpService("http://localhost:8545"));
		Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/v3/ec4079db317948a4b9457eb62b8fc0f4"));
		String nodePrivateKey = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.nodePrivateKey")
		Credentials credentials = Credentials.create(nodePrivateKey)
		ENS ens = ENS.load(Contracts.MAINNET, web3j, credentials, null)
		byte[] node = NameHash.nameHashAsBytes(domain)
		RemoteCall<String> request = ens.owner(node)
		String owner = request.send()
		return owner
	}
}
