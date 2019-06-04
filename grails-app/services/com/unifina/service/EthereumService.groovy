package com.unifina.service

import com.unifina.signalpath.blockchain.Web3jHelper
import org.web3j.abi.datatypes.Utf8String
import org.web3j.protocol.Web3j

class EthereumService {
	String fetchJoinPartStreamID(String communityAddress) {
		Web3j web3j = Web3jHelper.getWeb3jConnectionFromConfig()
		try {
			return Web3jHelper.getPublicField(web3j, communityAddress, "getJoinPartStream", Utf8String.class)
		} catch (IOException e) {
			throw new RuntimeException(e)
		}
	}

	/**
	 * Calls operator() getter from contract with public "operator" address:
	 * address public operator;
	 */
	String fetchCommunityAdminsEthereumAddress(String communityAddress) {
		Web3j web3j = Web3jHelper.getWeb3jConnectionFromConfig()
		try {
			return Web3jHelper.getPublicField(web3j, communityAddress, "owner", Utf8String.class)
		} catch (IOException e) {
			throw new RuntimeException(e)
		}
	}
}
