package com.unifina.controller

import com.unifina.api.BadRequestException
import com.unifina.domain.EthereumAddressValidator
import com.unifina.service.DataUnionService

class DataUnionApiController {
	DataUnionService dataUnionService
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
		DataUnionService.ProxyResponse result = dataUnionService.stats(contractAddress)
		response.status = result.statusCode
		render(text: result.body, contentType: "application/json", encoding: "UTF-8")
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def members(String contractAddress) {
		if (!isDataUnionAddress(contractAddress)) {
			throw new BadRequestException("Data Union address is not an Ethereum address")
		}
		DataUnionService.ProxyResponse result = dataUnionService.members(contractAddress)
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
		DataUnionService.ProxyResponse result = dataUnionService.memberStats(contractAddress, memberAddress)
		response.status = result.statusCode
		render(text: result.body, contentType: "application/json", encoding: "UTF-8")
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def summary() {
		DataUnionService.ProxyResponse result = dataUnionService.summary()
		response.status = result.statusCode
		render(text: result.body, contentType: "application/json", encoding: "UTF-8")
	}
}
