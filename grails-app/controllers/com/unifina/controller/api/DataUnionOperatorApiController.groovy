package com.unifina.controller.api

import com.unifina.api.BadRequestException
import com.unifina.service.DataUnionOperatorService
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.utils.EthereumAddressValidator

class DataUnionOperatorApiController {
	DataUnionOperatorService dataUnionOperatorService
	static boolean isDataUnionAddress(String value) {
		return EthereumAddressValidator.validate(value)
	}
	static boolean isMemberAddress(String value) {
		return EthereumAddressValidator.validate(value)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def stats(String contractAddress) {
		if (!isDataUnionAddress(contractAddress)) {
			throw new BadRequestException("Data Union address is not an Ethereum address")
		}
		DataUnionOperatorService.ProxyResponse result = dataUnionOperatorService.stats(contractAddress)
		response.status = result.statusCode
		render(text: result.body, contentType: "application/json", encoding: "UTF-8")
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def members(String contractAddress) {
		if (!isDataUnionAddress(contractAddress)) {
			throw new BadRequestException("Data Union address is not an Ethereum address")
		}
		DataUnionOperatorService.ProxyResponse result = dataUnionOperatorService.members(contractAddress)
		response.status = result.statusCode
		render(text: result.body, contentType: "application/json", encoding: "UTF-8")
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def memberStats(String contractAddress, String memberAddress) {
		if (!isDataUnionAddress(contractAddress)) {
			throw new BadRequestException("Data Union address is not an Ethereum address")
		}
		if (!isMemberAddress(memberAddress)) {
			throw new BadRequestException("Member address is not an Ethereum address")
		}
		DataUnionOperatorService.ProxyResponse result = dataUnionOperatorService.memberStats(contractAddress, memberAddress)
		response.status = result.statusCode
		render(text: result.body, contentType: "application/json", encoding: "UTF-8")
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def summary() {
		DataUnionOperatorService.ProxyResponse result = dataUnionOperatorService.summary()
		response.status = result.statusCode
		render(text: result.body, contentType: "application/json", encoding: "UTF-8")
	}
}
