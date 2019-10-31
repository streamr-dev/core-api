package com.unifina.service

import com.unifina.api.BadRequestException
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
		result.secret = cmd.secret ?: generator.generate()
		result.save(validate: true, failOnError: true)
		return result
	}

	CommunitySecret find(String communityAddress, String communitySecretId) {
		CommunitySecret result = CommunitySecret.where {
			(communityAddress == communityAddress) && (id == communitySecretId)
		}.find()
		return result
	}

	CommunitySecret update(String communityAddress, String communitySecretId, CommunitySecretCommand cmd) {
		CommunitySecret result = CommunitySecret.findWhere(communityAddress: communityAddress, id: communitySecretId)
		if (result == null) {
			throw new NotFoundException("Community secret not found")
		}
		if (cmd.name) {
			result.name = cmd.name
		}
		if (cmd.secret) {
			throw new BadRequestException("Can't change the secret, please create a new community secret")
		}
		result.save(validate: true, failOnError: true)
		return result
	}

	void delete(String communityAddress, String communitySecretId) {
		CommunitySecret result = CommunitySecret.findWhere(communityAddress: communityAddress, id: communitySecretId)
		if (result == null) {
			throw new NotFoundException("Community secret not found")
		}
		result.delete(flush: true)
	}
}
