package com.unifina.service

import com.unifina.api.CommunitySecretCommand
import com.unifina.api.NotFoundException
import com.unifina.domain.community.CommunitySecret

class CommunitySecretService {
	List<CommunitySecret> findCommunitySecrets(String communityAddress) {
		return CommunitySecret.findAllWhere(communityAddress: communityAddress)
	}

	CommunitySecret createCommunitySecret(String communityAddress, CommunitySecretCommand cmd) {
		CommunitySecret result = new CommunitySecret()
		result.communityAddress = communityAddress
		result.name = cmd.name
		result.secret = cmd.secret
		result.save(validate: true, failOnError: true)
		return result
	}

	CommunitySecret findCommunitySecret(String communityAddress, String communitySecretId) {
		CommunitySecret result = CommunitySecret.where {
			(communityAddress == communityAddress) && (id == communitySecretId)
		}.find()
		return result
	}

	CommunitySecret updateCommunitySecret(String communityAddress, String communitySecretId, CommunitySecretCommand cmd) {
		CommunitySecret result = CommunitySecret.findWhere(communityAddress: communityAddress, id: communitySecretId)
		if (result == null) {
			return null
		}
		result.name = cmd.name
		result.save(validate: true, failOnError: true)
		return result
	}

	void deleteCommunitySecret(String communityAddress, String communitySecretId) {
		CommunitySecret result = CommunitySecret.findWhere(communityAddress: communityAddress, id: communitySecretId)
		if (result == null) {
			throw new NotFoundException("community secret not found by id")
		}
		result.delete(flush: true)
	}
}
