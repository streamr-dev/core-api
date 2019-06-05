package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.api.BadRequestException
import com.unifina.api.CommunitySecretCommand
import com.unifina.api.NotFoundException
import com.unifina.domain.community.CommunitySecret
import com.unifina.domain.security.SecUser
import com.unifina.security.StreamrApi
import com.unifina.service.CommunitySecretService
import com.unifina.service.CommunityService
import com.unifina.utils.EthereumAddressValidator
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class CommunitySecretApiController {
	CommunitySecretService communitySecretService
	CommunityService communityService

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

	void checkAdminAccessControl(SecUser user, String communityAddress) {
		if (!communityService.checkAdminAccessControl(user, communityAddress)) {
			throw new ApiException(403, "ACCESS_DENIED", "required admin role is missing")
		}
	}

	// curl -v -H "Authorization: token tester1-api-key" http://localhost:8081/streamr-core/api/v1/communities/0x6c90aece04198da2d5ca9b956b8f95af8041de37/secrets
	@StreamrApi
	def findAll(String communityAddress) {
		if (!isCommunityAddress(communityAddress)) {
			throw new BadRequestException("community address is not an ethereum address")
		}
		checkAdminAccessControl(loggedInUser(), communityAddress)
		List<CommunitySecret> secrets = communitySecretService.findAll(communityAddress)
		render(secrets*.toMap() as JSON)
	}

	// curl -v -H "Authorization: token tester1-api-key" -H "Content-Type: application/json" -d '{"name":"name"}' http://localhost:8081/streamr-core/api/v1/communities/0x6c90aece04198da2d5ca9b956b8f95af8041de37/secrets
	@StreamrApi
	def create(String communityAddress, CommunitySecretCommand cmd) {
		if (!isCommunityAddress(communityAddress)) {
			throw new BadRequestException("community address is not an ethereum address")
		}
		if (cmd.errors.getFieldError("name")) {
			throw new BadRequestException("name in json is not a valid name")
		}
		checkAdminAccessControl(loggedInUser(), communityAddress)
		CommunitySecret secret = communitySecretService.create(communityAddress, cmd)
		render(secret.toMap() as JSON)
	}

	// curl -v -H "Authorization: token tester1-api-key" http://localhost:8081/streamr-core/api/v1/communities/0x6c90aece04198da2d5ca9b956b8f95af8041de37/secrets/L-TvrBkyQTS_JK1ABHFEZAaZ3FHq7-TPqMXe9JNz1x6g
	@StreamrApi
	def find(String communityAddress, String communitySecretId) {
		if (!isCommunityAddress(communityAddress)) {
			throw new BadRequestException("community address is not an ethereum address")
		}
		if (!isValidID(communitySecretId)) {
			throw new BadRequestException("community secret id is not valid")
		}
		checkAdminAccessControl(loggedInUser(), communityAddress)
		CommunitySecret secret = communitySecretService.find(communityAddress, communitySecretId)
		if (secret == null) {
			throw new NotFoundException("community secret not found by id")
		}
		render(secret.toMap() as JSON)
	}

	// curl -v -X PUT -H "Authorization: token tester1-api-key" -H "Content-Type: application/json" -d '{"name":"new name"}' http://localhost:8081/streamr-core/api/v1/communities/0x6c90aece04198da2d5ca9b956b8f95af8041de37/secrets/L-TvrBkyQTS_JK1ABHFEZAaZ3FHq7-TPqMXe9JNz1x6g
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
		checkAdminAccessControl(loggedInUser(), communityAddress)
		CommunitySecret secret = communitySecretService.update(communityAddress, communitySecretId, cmd)
		if (secret == null) {
			throw new NotFoundException("community secret not found by id")
		}
		render(secret?.toMap() as JSON)
	}

	// curl -v -X DELETE -H "Authorization: token tester1-api-key" http://localhost:8081/streamr-core/api/v1/communities/0x6c90aece04198da2d5ca9b956b8f95af8041de37/secrets/L-TvrBkyQTS_JK1ABHFEZAaZ3FHq7-TPqMXe9JNz1x6g
	@StreamrApi
	def delete(String communityAddress, String communitySecretId) {
		if (!isCommunityAddress(communityAddress)) {
			throw new BadRequestException("community address is not an ethereum address")
		}
		if (!isValidID(communitySecretId)) {
			throw new BadRequestException("community secret id is not valid")
		}
		checkAdminAccessControl(loggedInUser(), communityAddress)
		communitySecretService.delete(communityAddress, communitySecretId)
		render(status: 204)
	}

	private SecUser loggedInUser() {
		return (SecUser) request.apiUser
	}
}
