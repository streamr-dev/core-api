package com.unifina.controller.api


import com.unifina.api.BadRequestException
import com.unifina.api.CommunitySecretCommand
import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.domain.community.CommunitySecret
import com.unifina.domain.security.SecUser
import com.unifina.security.StreamrApi
import com.unifina.service.CommunitySecretService

import com.unifina.service.EthereumService
import com.unifina.utils.EthereumAddressValidator
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class CommunitySecretApiController {
	CommunitySecretService communitySecretService
	EthereumService ethereumService

	private SecUser loggedInUser() {
		return (SecUser) request.apiUser
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

	void checkAdminAccessControl(SecUser user, String communityAddress) {
		if (!EthereumAddressValidator.validate(communityAddress)) {
			throw new BadRequestException("Community address is not a valid Ethereum address")
		}
		String adminAddress = ethereumService.fetchCommunityAdminsEthereumAddress(communityAddress)
		if (adminAddress == null) {
			throw new BadRequestException("Community address is not of a Community smart contract")
		}
		if (!ethereumService.hasEthereumAddress(user, adminAddress)) {
			throw new NotPermittedException(user?.username, "community", communityAddress, "manage")
		}
	}

	// curl -v -H "Authorization: token tester1-api-key" http://localhost:8081/streamr-core/api/v1/communities/0x6c90aece04198da2d5ca9b956b8f95af8041de37/secrets
	@StreamrApi
	def index(String communityAddress) {
		checkAdminAccessControl(loggedInUser(), communityAddress)
		List<CommunitySecret> secrets = communitySecretService.findAll(communityAddress)
		render(secrets*.toMap() as JSON)
	}

	// curl -v -H "Authorization: token tester1-api-key" -H "Content-Type: application/json" -d '{"name":"name"}' http://localhost:8081/streamr-core/api/v1/communities/0x6c90aece04198da2d5ca9b956b8f95af8041de37/secrets
	@StreamrApi
	def save(String communityAddress, CommunitySecretCommand cmd) {
		checkAdminAccessControl(loggedInUser(), communityAddress)
		if (cmd.errors.getFieldError("name")) {
			throw new BadRequestException("name in json is not a valid name")
		}
		CommunitySecret secret = communitySecretService.create(communityAddress, cmd)
		render(secret.toMap() as JSON)
	}

	// curl -v -H "Authorization: token tester1-api-key" http://localhost:8081/streamr-core/api/v1/communities/0x6c90aece04198da2d5ca9b956b8f95af8041de37/secrets/L-TvrBkyQTS_JK1ABHFEZAaZ3FHq7-TPqMXe9JNz1x6g
	@StreamrApi
	def show(String communityAddress, String id) {
		checkAdminAccessControl(loggedInUser(), communityAddress)
		if (!isValidID(id)) {
			throw new BadRequestException("community secret id is not valid")
		}
		CommunitySecret secret = communitySecretService.find(communityAddress, id)
		if (secret == null) {
			throw new NotFoundException("community secret not found by id")
		}
		render(secret.toMap() as JSON)
	}

	// curl -v -X PUT -H "Authorization: token tester1-api-key" -H "Content-Type: application/json" -d '{"name":"new name"}' http://localhost:8081/streamr-core/api/v1/communities/0x6c90aece04198da2d5ca9b956b8f95af8041de37/secrets/L-TvrBkyQTS_JK1ABHFEZAaZ3FHq7-TPqMXe9JNz1x6g
	@StreamrApi
	def update(String communityAddress, String id, CommunitySecretCommand cmd) {
		checkAdminAccessControl(loggedInUser(), communityAddress)
		if (!isValidID(id)) {
			throw new BadRequestException("community secret id is not valid")
		}
		if (cmd.errors.getFieldError("name")) {
			throw new BadRequestException("name in json is not a valid name")
		}
		CommunitySecret secret = communitySecretService.update(communityAddress, id, cmd)
		render(secret.toMap() as JSON)
	}

	// curl -v -X DELETE -H "Authorization: token tester1-api-key" http://localhost:8081/streamr-core/api/v1/communities/0x6c90aece04198da2d5ca9b956b8f95af8041de37/secrets/L-TvrBkyQTS_JK1ABHFEZAaZ3FHq7-TPqMXe9JNz1x6g
	@StreamrApi
	def delete(String communityAddress, String id) {
		checkAdminAccessControl(loggedInUser(), communityAddress)
		if (!isValidID(id)) {
			throw new BadRequestException("community secret id is not valid")
		}
		communitySecretService.delete(communityAddress, id)
		render(status: 204)
	}
}
