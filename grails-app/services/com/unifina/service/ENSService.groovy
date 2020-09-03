package com.unifina.service

import com.unifina.domain.User
import com.unifina.utils.Web3jWrapper

class ENSService {
	static transactional = false

	Web3jWrapper web3jWrapper = new Web3jWrapper()

	boolean isENSOwnedBy(String domain, User user) {
		if (!user.isEthereumUser()) {
			throw new EthereumUserRequiredException()
		}
		String owner = web3jWrapper.getENSDomainOwner(domain)
		return user.username == owner
	}
}

