package com.streamr.core.service

import com.streamr.core.domain.User
import com.streamr.core.utils.ApplicationConfig
import com.streamr.core.utils.EthereumConfig
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.web3j.abi.datatypes.Address
import org.web3j.protocol.Web3j

class EthereumService {
	private static final Logger log = LogManager.getLogger(EthereumService.class)

	/**
	 * Calls owner() getter from data union contract
	 * @return admin's address, or null if contractAddress is faulty
	 */
	String fetchDataUnionAdminsEthereumAddress(String contractAddress) {
		EthereumConfig ethereumConf = new EthereumConfig(ApplicationConfig.getString("streamr.ethereum.defaultNetwork"));
		Web3j web3j = ethereumConf.getWeb3j(EthereumConfig.RpcConnectionMethod.HTTP)
		try {
			return Web3jHelper.getPublicField(web3j, contractAddress, "owner", Address.class)
		} catch (IOException e) {
			log.error("fetch data union admins ethereum address error", e)
			throw new BlockchainException(e)
		} finally {
			if (web3j != null) {
				web3j.shutdown()
			}
		}
	}

	/** Checks if given user has registered the given Ethereum address in their Streamr profile */
	boolean hasEthereumAddress(User user, String ethereumAddress) {
		User u = User.createCriteria().get() {
			eq("username", user.getUsername(), [ignoreCase: true])
		}
		return u != null
	}
}
