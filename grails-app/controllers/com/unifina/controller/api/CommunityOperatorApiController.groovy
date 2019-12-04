package com.unifina.controller.api

import com.unifina.api.BadRequestException
import com.unifina.service.CommunityOperatorService
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.utils.EthereumAddressValidator
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class CommunityOperatorApiController {
	CommunityOperatorService communityOperatorService
	static boolean isCommunityAddress(String value) {
		return EthereumAddressValidator.validate(value)
	}
	static boolean isMemberAddress(String value) {
		return EthereumAddressValidator.validate(value)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def stats(String communityAddress) {
		if (!isCommunityAddress(communityAddress)) {
			throw new BadRequestException("Community address is not an ethereum address")
		}
		CommunityOperatorService.ProxyResponse result = communityOperatorService.stats(communityAddress)
		response.status = result.statusCode
		render(text: result.body, contentType: "application/json", encoding: "UTF-8")
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def members(String communityAddress) {
		if (!isCommunityAddress(communityAddress)) {
			throw new BadRequestException("Community address is not an ethereum address")
		}
		CommunityOperatorService.ProxyResponse result = communityOperatorService.members(communityAddress)
		response.status = result.statusCode
		render(text: result.body, contentType: "application/json", encoding: "UTF-8")
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def memberStats(String communityAddress, String memberAddress) {
		if (!isCommunityAddress(communityAddress)) {
			throw new BadRequestException("Community address is not an ethereum address")
		}
		if (!isMemberAddress(memberAddress)) {
			throw new BadRequestException("Member address is not an ethereum address")
		}
		CommunityOperatorService.ProxyResponse result = communityOperatorService.memberStats(communityAddress, memberAddress)
		response.status = result.statusCode
		render(text: result.body, contentType: "application/json", encoding: "UTF-8")
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def summary() {
		CommunityOperatorService.ProxyResponse result = communityOperatorService.summary()
		response.status = result.statusCode
		render(text: result.body, contentType: "application/json", encoding: "UTF-8")
	}
}
