package com.unifina.controller

import com.unifina.domain.DataUnionJoinRequest
import com.unifina.domain.EthereumAddressValidator
import com.unifina.domain.IDValidator
import com.unifina.domain.User
import com.unifina.service.*
import grails.converters.JSON

class DataUnionJoinRequestApiController {
	DataUnionJoinRequestService dataUnionJoinRequestService
	EthereumService ethereumService

	private User loggedInUser() {
		return (User) request.apiUser
	}

	static boolean isDataUnionAddress(String value) {
		return EthereumAddressValidator.validate(value)
	}

	static boolean isValidID(String value) {
		return IDValidator.validate(value)
	}

	/**
	 * Publicly available endpoint: POST /dataunions/{contractAddress}/joinRequests
	 *
	 * Example:
	 * curl -v -H "Authorization: token tester1-api-key" -H "Content-Type: application/json" -d '{"memberAddress": "0x9334f0aa74d2744b97b0b1be6896788ee46f4aaa", metadata: {"foo":"bar"}}' http://localhost:8081/streamr-core/api/v1/dataunions/0x6c90aece04198da2d5ca9b956b8f95af8041de37/joinRequests
	 **/
	@StreamrApi
	def save(String contractAddress, DataUnionJoinRequestCommand cmd) {
		if (!isDataUnionAddress(contractAddress)) {
			throw new BadRequestException("Data Union address is not a valid Ethereum address")
		}
		if (cmd.errors.getFieldError("memberAddress")) {
			throw new BadRequestException("memberAddress in json is not an Ethereum address")
		}
		DataUnionJoinRequest result = dataUnionJoinRequestService.create(contractAddress, cmd, loggedInUser())
		render(result?.toMap() as JSON)
	}

	/** Admin endpoints below **/

	void checkAdminAccessControl(User user, String contractAddress) {
		if (!isDataUnionAddress(contractAddress)) {
			throw new BadRequestException("Data Union address is not a valid Ethereum address")
		}
		String adminAddress = ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress)
		if (adminAddress == null) {
			throw new BadRequestException("Data Union address is not of a data union smart contract")
		}
	}

	// curl -v -X GET -H "Authorization: token tester1-api-key" "http://localhost:8081/streamr-core/api/v1/dataunions/0x6c90aece04198da2d5ca9b956b8f95af8041de37/joinRequests?state=pending"
	@StreamrApi
	def index(String contractAddress, String state) {
		checkAdminAccessControl(loggedInUser(), contractAddress)
		DataUnionJoinRequest.State st = DataUnionJoinRequest.State.isState(state)
		List<DataUnionJoinRequest> results = dataUnionJoinRequestService.findAll(contractAddress, st)
		render(results*.toMap() as JSON)
	}

	// curl -v -X GET -H "Authorization: token tester1-api-key" http://localhost:8081/streamr-core/api/v1/dataunions/0x6c90aece04198da2d5ca9b956b8f95af8041de37/joinRequests/L-TvrBkyQTS_JK1ABHFEZAaZ3FHq7-TPqMXe9JNz1x6g
	@StreamrApi
	def show(String contractAddress, String id) {
		checkAdminAccessControl(loggedInUser(), contractAddress)
		if (!isValidID(id)) {
			throw new BadRequestException("join request id not valid")
		}
		DataUnionJoinRequest result = dataUnionJoinRequestService.find(contractAddress, id)
		if (result == null) {
			throw new NotFoundException("Data Union join request not found with id: " + id)
		}
		render(result.toMap() as JSON)
	}

	// curl -v -X PUT -H "Authorization: token tester1-api-key" -H "Content-Type: application/json" -d '{"state": "ACCEPTED"}' http://localhost:8081/streamr-core/api/v1/dataunions/0x6c90aece04198da2d5ca9b956b8f95af8041de37/joinRequests/L-TvrBkyQTS_JK1ABHFEZAaZ3FHq7-TPqMXe9JNz1x6g
	@StreamrApi
	def update(String contractAddress, String id, DataUnionUpdateJoinRequestCommand cmd) {
		checkAdminAccessControl(loggedInUser(), contractAddress)
		if (!isValidID(id)) {
			throw new BadRequestException("join request id not valid")
		}
		if (cmd.errors.getFieldError("state")) {
			throw new BadRequestException("state in json is not valid")
		}
		DataUnionJoinRequest result = dataUnionJoinRequestService.update(contractAddress, id, cmd)
		render(result?.toMap() as JSON)
	}

	// curl -v -X DELETE -H "Authorization: token tester1-api-key" http://localhost:8081/streamr-core/api/v1/dataunions/0x6c90aece04198da2d5ca9b956b8f95af8041de37/joinRequests/L-TvrBkyQTS_JK1ABHFEZAaZ3FHq7-TPqMXe9JNz1x6g
	@StreamrApi
	def delete(String contractAddress, String id) {
		checkAdminAccessControl(loggedInUser(), contractAddress)
		if (!isValidID(id)) {
			throw new BadRequestException("join request id not valid")
		}
		dataUnionJoinRequestService.delete(contractAddress, id)
		render(status: 204)
	}
}
