package com.unifina.controller.api

import com.unifina.api.BadRequestException
import com.unifina.api.CommunityJoinRequestCommand
import com.unifina.api.NotFoundException
import com.unifina.api.UpdateCommunityJoinRequestCommand
import com.unifina.domain.security.SecUser
import com.unifina.security.StreamrApi
import com.unifina.domain.community.CommunityJoinRequest
import com.unifina.service.CommunityProductService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class CommunityProductApiController {
	CommunityProductService communityProductService

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
	static boolean isEthereumAddress(String value) {
		return value ==~ /^0x[a-fA-F0-9]{40}$/
	}
	static boolean isCommunityAddress(String value) {
		return isEthereumAddress(value)
	}
	static boolean isJoinRequestId(String value) {
		if (value == null) {
			return false
		}
		return true
	}

	// curl -v -X GET -H "Authorization: token tester1-api-key" "http://localhost:8081/streamr-core/api/v1/communities/0x6c90aece04198da2d5ca9b956b8f95af8041de37/joinRequests?state=pending"
	@StreamrApi
    def findCommunityJoinRequests(String communityAddress, String state) {
		if (!isCommunityAddress(communityAddress)) {
			throw new BadRequestException("community address is not an ethereum address")
		}
		CommunityJoinRequest.State st = isState(state)
		List<CommunityJoinRequest> results = communityProductService.findCommunityJoinRequests(communityAddress, st)
		render(results*.toMap() as JSON)
	}

	// curl -v -H "Authorization: token tester1-api-key" -H "Content-Type: application/json" -d '{"memberAddress": "0x9334f0aa74d2744b97b0b1be6896788ee46f4aaa", "secret": "secret", metadata: {"foo":"bar"}}' http://localhost:8081/streamr-core/api/v1/communities/0x6c90aece04198da2d5ca9b956b8f95af8041de37/joinRequests
	@StreamrApi
	def createCommunityJoinRequest(String communityAddress, CommunityJoinRequestCommand cmd) {
		if (!isCommunityAddress(communityAddress)) {
			throw new BadRequestException("community address is not an ethereum address")
		}
		if (cmd.errors.getFieldError("memberAddress")) {
			throw new BadRequestException("memberAddress in json is not an ethereum address")
		}
		CommunityJoinRequest result = communityProductService.createCommunityJoinRequest(communityAddress, cmd, loggedInUser())
		render(result?.toMap() as JSON)
	}

	// curl -v -X GET -H "Authorization: token tester1-api-key" http://localhost:8081/streamr-core/api/v1/communities/0x6c90aece04198da2d5ca9b956b8f95af8041de37/joinRequests/community-join-request-id
	@StreamrApi
	def findCommunityJoinRequest(String communityAddress, String joinRequestId) {
		if (!isCommunityAddress(communityAddress)) {
			throw new BadRequestException("community address is not an ethereum address")
		}
		if (!isJoinRequestId(joinRequestId)) {
			throw new BadRequestException("join request id not valid")
		}
		CommunityJoinRequest result = communityProductService.findCommunityJoinRequest(communityAddress, joinRequestId)
		if (result == null) {
			throw new NotFoundException("community join request not found with id: " + joinRequestId)
		}
		render(result.toMap() as JSON)
	}

	// curl -v -X PUT -H "Authorization: token tester1-api-key" -H "Content-Type: application/json" -d '{"state": "ACCEPTED"}' http://localhost:8081/streamr-core/api/v1/communities/0x6c90aece04198da2d5ca9b956b8f95af8041de37/joinRequests/community-join-request-id
	@StreamrApi
	def updateCommunityJoinRequest(String communityAddress, String joinRequestId, UpdateCommunityJoinRequestCommand cmd) {
		if (!isCommunityAddress(communityAddress)) {
			throw new BadRequestException("community address is not an ethereum address")
		}
		if (!isJoinRequestId(joinRequestId)) {
			throw new BadRequestException("join request id not valid")
		}
		if (cmd.errors.getFieldError("state")) {
			throw new BadRequestException("state in json is not valid")
		}
		CommunityJoinRequest result = communityProductService.updateCommunityJoinRequest(communityAddress, joinRequestId, cmd)
		if (result == null) {
			throw new NotFoundException("community join request not found with id: " + joinRequestId)
		}
		render(status: 204)
	}

	private SecUser loggedInUser() {
		return (SecUser) request.apiUser
	}
}
