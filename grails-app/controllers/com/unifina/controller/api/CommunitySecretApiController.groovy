package com.unifina.controller.api

import com.unifina.api.BadRequestException
import com.unifina.api.CommunitySecretCommand
import com.unifina.api.NotFoundException
import com.unifina.domain.community.CommunitySecret
import com.unifina.security.StreamrApi
import com.unifina.service.CommunitySecretService
import com.unifina.utils.EthereumAddressValidator
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class CommunitySecretApiController {
	CommunitySecretService communitySecretService

	static boolean isCommunityAddress(String value) {
		return EthereumAddressValidator.validate(value)
	}
	static boolean isValidID(String value) {
		if (value == null) {
			return false
		}
		if (value.length() != 44) {
			return false
		}
		return value ==~ /^[a-zA-Z0-9-_]{44}$/
	}

	@StreamrApi
	def findAll(String communityAddress) {
		if (!isCommunityAddress(communityAddress)) {
			throw new BadRequestException("community address is not an ethereum address")
		}
		List<CommunitySecret> secrets = communitySecretService.findAll(communityAddress)
		render(secrets*.toMap() as JSON)
	}

	@StreamrApi
	def create(String communityAddress, CommunitySecretCommand cmd) {
		if (!isCommunityAddress(communityAddress)) {
			throw new BadRequestException("community address is not an ethereum address")
		}
		if (cmd.errors.getFieldError("name")) {
			throw new BadRequestException("name in json is not a valid name")
		}
		CommunitySecret secret = communitySecretService.create(communityAddress, cmd)
		render(secret.toMap() as JSON)
	}

	@StreamrApi
	def find(String communityAddress, String communitySecretId) {
		if (!isCommunityAddress(communityAddress)) {
			throw new BadRequestException("community address is not an ethereum address")
		}
		if (!isValidID(communitySecretId)) {
			throw new BadRequestException("community secret id is not valid")
		}
		CommunitySecret secret = communitySecretService.find(communityAddress, communitySecretId)
		if (secret == null) {
			throw new NotFoundException("community secret not found by id")
		}
		render(secret.toMap() as JSON)
	}

	@StreamrApi
	def update(String communityAddress, String communitySecretId, CommunitySecretCommand cmd) {
		if (!isCommunityAddress(communityAddress)) {
			throw new BadRequestException("community address is not an ethereum address")
		}
		if (!isValidID(communitySecretId)) {
			throw new BadRequestException("community secret id is not valid")
		}
		if (cmd.errors.getFieldError("name")) {
			throw new BadRequestException("name in json is not a valid name")
		}
		CommunitySecret secret = communitySecretService.update(communityAddress, communitySecretId, cmd)
		if (secret == null) {
			throw new NotFoundException("community secret not found by id")
		}
		render(secret?.toMap() as JSON)
	}

	@StreamrApi
	def delete(String communityAddress, String communitySecretId) {
		if (!isCommunityAddress(communityAddress)) {
			throw new BadRequestException("community address is not an ethereum address")
		}
		if (!isValidID(communitySecretId)) {
			throw new BadRequestException("community secret id is not valid")
		}
		communitySecretService.delete(communityAddress, communitySecretId)
		render(status: 204)
	}
}
