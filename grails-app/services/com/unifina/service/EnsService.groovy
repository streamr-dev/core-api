package com.unifina.service

import com.unifina.domain.User
import com.unifina.utils.Web3jHelper

class EnsService {
	static transactional = false
	EthereumUserService ethereumUserService

	boolean isENSOwnedBy(String domain, User expectedOwner) {
		String actualOwnerAddress = Web3jHelper.getENSDomainOwner(domain)
		User actualOwner = ethereumUserService.getEthereumUser(actualOwnerAddress)
		return (actualOwner != null) && (actualOwner.id == expectedOwner.id)
	}
}

