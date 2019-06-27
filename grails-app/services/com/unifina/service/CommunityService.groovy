package com.unifina.service

import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser

class CommunityService {
	EthereumService ethereumService

	boolean checkAdminAccessControl(SecUser user, String communityAddress) {
		String adminAddress = ethereumService.fetchCommunityAdminsEthereumAddress(communityAddress)
		IntegrationKey key = IntegrationKey.where {
			(user == user) && (idInService == adminAddress)
		}.find()
		return key != null
	}
}
