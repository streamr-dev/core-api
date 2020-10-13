package com.unifina.service

import com.unifina.domain.User
import com.unifina.signalpath.blockchain.Web3jHelper

class EnsService {
	static transactional = false
	EthereumIntegrationKeyService ethereumIntegrationKeyService

	boolean isENSOwnedBy(String domain, User expectedOwner) {
		if (!expectedOwner.isEthereumUser()) {
			throw new EthereumUserRequiredException()
		}
		String actualOwnerAddress = Web3jHelper.getENSDomainOwner(domain)
		User actualOwner = ethereumIntegrationKeyService.getEthereumUser(actualOwnerAddress)
		return (actualOwner != null) && (actualOwner.id == expectedOwner.id)
	}
}

