package com.unifina.controller.api

import com.unifina.api.*
import com.unifina.domain.community.CommunityJoinRequest
import com.unifina.domain.security.SecUser
import com.unifina.security.StreamrApi
import com.unifina.service.CommunityJoinRequestService
import com.unifina.service.CommunityService
import com.unifina.utils.EthereumAddressValidator
import com.unifina.utils.IDValidator
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class CommunityJoinRequestApiController {
	CommunityJoinRequestService communityJoinRequestService
	CommunityService communityService

	static CommunityJoinRequest.State isState(String value) {
		if (value == null || value.trim().equals("")) {
			return null
		}
		try {
			return CommunityJoinRequest.State.valueOf(value.toUpperCase())
		} catch (IllegalArgumentException e) {
			return null
		}
	}
	static boolean isCommunityAddress(String value) {
		return EthereumAddressValidator.validate(value)
	}
	static boolean isValidID(String value) {
		return IDValidator.validate(value)
	}

	void checkAdminAccessControl(SecUser user, String communityAddress) {
		if (!communityService.checkAdminAccessControl(user, communityAddress)) {
			throw new ApiException(403, "ACCESS_DENIED", "User is not the admin of the community")
		}
	}

	// curl -v -X GET -H "Authorization: token tester1-api-key" "http://localhost:8081/streamr-core/api/v1/communities/0x6c90aece04198da2d5ca9b956b8f95af8041de37/joinRequests?state=pending"
	@StreamrApi
    def findAll(String communityAddress, String state) {
		if (!isCommunityAddress(communityAddress)) {
			throw new BadRequestException("community address is not an ethereum address")
		}
		checkAdminAccessControl(loggedInUser(), communityAddress)
		CommunityJoinRequest.State st = isState(state)
		List<CommunityJoinRequest> results = communityJoinRequestService.findAll(communityAddress, st)
		render(results*.toMap() as JSON)
	}

	// curl -v -H "Authorization: token tester1-api-key" -H "Content-Type: application/json" -d '{"memberAddress": "0x9334f0aa74d2744b97b0b1be6896788ee46f4aaa", "secret": "secret", metadata: {"foo":"bar"}}' http://localhost:8081/streamr-core/api/v1/communities/0x6c90aece04198da2d5ca9b956b8f95af8041de37/joinRequests
	@StreamrApi
	def create(String communityAddress, CommunityJoinRequestCommand cmd) {
		if (!isCommunityAddress(communityAddress)) {
			throw new BadRequestException("community address is not an ethereum address")
		}
		if (cmd.errors.getFieldError("memberAddress")) {
			throw new BadRequestException("memberAddress in json is not an ethereum address")
		}
		CommunityJoinRequest result = communityJoinRequestService.create(communityAddress, cmd, loggedInUser())
		render(result?.toMap() as JSON)
	}

	// curl -v -X GET -H "Authorization: token tester1-api-key" http://localhost:8081/streamr-core/api/v1/communities/0x6c90aece04198da2d5ca9b956b8f95af8041de37/joinRequests/L-TvrBkyQTS_JK1ABHFEZAaZ3FHq7-TPqMXe9JNz1x6g
	@StreamrApi
	def find(String communityAddress, String joinRequestId) {
		if (!isCommunityAddress(communityAddress)) {
			throw new BadRequestException("community address is not an ethereum address")
		}
		if (!isValidID(joinRequestId)) {
			throw new BadRequestException("join request id not valid")
		}
		checkAdminAccessControl(loggedInUser(), communityAddress)
		CommunityJoinRequest result = communityJoinRequestService.find(communityAddress, joinRequestId)
		if (result == null) {
			throw new NotFoundException("community join request not found with id: " + joinRequestId)
		}
		render(result.toMap() as JSON)
	}

	// curl -v -X PUT -H "Authorization: token tester1-api-key" -H "Content-Type: application/json" -d '{"state": "ACCEPTED"}' http://localhost:8081/streamr-core/api/v1/communities/0x6c90aece04198da2d5ca9b956b8f95af8041de37/joinRequests/L-TvrBkyQTS_JK1ABHFEZAaZ3FHq7-TPqMXe9JNz1x6g
	@StreamrApi
	def update(String communityAddress, String joinRequestId, UpdateCommunityJoinRequestCommand cmd) {
		if (!isCommunityAddress(communityAddress)) {
			throw new BadRequestException("community address is not an ethereum address")
		}
		if (!isValidID(joinRequestId)) {
			throw new BadRequestException("join request id not valid")
		}
		if (cmd.errors.getFieldError("state")) {
			throw new BadRequestException("state in json is not valid")
		}
		checkAdminAccessControl(loggedInUser(), communityAddress)
		CommunityJoinRequest result = communityJoinRequestService.update(communityAddress, joinRequestId, cmd)
		render(result?.toMap() as JSON)
	}

	private SecUser loggedInUser() {
		return (SecUser) request.apiUser
	}
}
