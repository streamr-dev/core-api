package com.unifina.service

import com.unifina.api.CommunitySecretCommand
import com.unifina.api.NotFoundException
import com.unifina.domain.community.CommunitySecret
import com.unifina.utils.IdGenerator

class CommunitySecretService {
	IdGenerator generator = new IdGenerator()

	List<CommunitySecret> findAll(String communityAddress) {
		return CommunitySecret.findAllWhere(communityAddress: communityAddress)
	}

	CommunitySecret create(String communityAddress, CommunitySecretCommand cmd) {
		CommunitySecret result = new CommunitySecret()
		result.communityAddress = communityAddress
		result.name = cmd.name
		result.secret = generator.generate()
		result.save(validate: true, failOnError: true)
		return result
	}

	CommunitySecret find(String communityAddress, String communitySecretId) {
		CommunitySecret result = CommunitySecret.where {
			(communityAddress == communityAddress) && (id == communitySecretId)
		}.find()
		return result
	}

	CommunitySecret updateCommunitySecret(String communityAddress, String communitySecretId, CommunitySecretCommand cmd) {
		CommunitySecret result = CommunitySecret.findWhere(communityAddress: communityAddress, id: communitySecretId)
		if (result == null) {
			throw new NotFoundException("community secret not found")
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
