package com.unifina.service

import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import com.unifina.signalpath.blockchain.Web3jHelper
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Utf8String
import org.web3j.protocol.Web3j

class EthereumService {
	private static final Logger log = LogManager.getLogger(EthereumService.class)

	String fetchJoinPartStreamID(String communityAddress) {
		Web3j web3j = Web3jHelper.getWeb3jConnectionFromConfig()
		try {
			return Web3jHelper.getPublicField(web3j, communityAddress, "joinPartStream", Utf8String.class)
		} catch (IOException e) {
			log.error("fetch community join part stream id error", e)
			throw new RuntimeException(e)
		}
	}

	/**
	 * Calls owner() getter from Community contract
	 * @return community admin's address, or null if communityAddress is faulty
	 */
	String fetchCommunityAdminsEthereumAddress(String communityAddress) {
		Web3j web3j = Web3jHelper.getWeb3jConnectionFromConfig()
		try {
			return Web3jHelper.getPublicField(web3j, communityAddress, "owner", Address.class)
		} catch (IOException e) {
			log.error("fetch community admins ethereum address error", e)
			throw new RuntimeException(e)
		}
	}

	/** Checks if given user has registered the given Ethereum address in their Streamr profile */
	boolean hasEthereumAddress(SecUser user, String ethereumAddress) {
		// TODO: should we check that service == "ETHEREUM"?
		IntegrationKey key = IntegrationKey.where {
			(user == user) && (idInService == ethereumAddress)
		}.find()
		return key != null
	}
}
