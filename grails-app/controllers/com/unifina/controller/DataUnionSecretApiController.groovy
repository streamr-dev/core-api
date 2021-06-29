package com.unifina.controller

import com.unifina.domain.DataUnionSecret
import com.unifina.domain.EthereumAddressValidator
import com.unifina.domain.User
import com.unifina.service.*
import grails.converters.JSON

class DataUnionSecretApiController {
	DataUnionSecretService dataUnionSecretService
	EthereumService ethereumService

	private User loggedInUser() {
		return (User) request.apiUser
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

	void checkAdminAccessControl(User user, String contractAddress) {
		if (!EthereumAddressValidator.validate(contractAddress)) {
			throw new BadRequestException("Data Union address is not a valid Ethereum address")
		}
		String adminAddress = ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress)
		if (adminAddress == null) {
			throw new BadRequestException("Data Union address is not of a data union smart contract")
		}
	}

	// curl -v -H "Authorization: token tester1-api-key" http://localhost:8081/streamr-core/api/v1/dataunions/0x6c90aece04198da2d5ca9b956b8f95af8041de37/secrets
	@StreamrApi
	def index(String contractAddress) {
		checkAdminAccessControl(loggedInUser(), contractAddress)
		List<DataUnionSecret> secrets = dataUnionSecretService.findAll(contractAddress)
		render(secrets*.toMap() as JSON)
	}

	// curl -v -H "Authorization: token tester1-api-key" -H "Content-Type: application/json" -d '{"name":"name"}' http://localhost:8081/streamr-core/api/v1/dataunions/0x6c90aece04198da2d5ca9b956b8f95af8041de37/secrets
	@StreamrApi
	def save(String contractAddress, DataUnionSecretCommand cmd) {
		checkAdminAccessControl(loggedInUser(), contractAddress)
		if (cmd.errors.getFieldError("name")) {
			throw new BadRequestException("name in json is not a valid name")
		}
		DataUnionSecret secret = dataUnionSecretService.create(contractAddress, cmd)
		render(secret.toMap() as JSON)
	}

	// curl -v -H "Authorization: token tester1-api-key" http://localhost:8081/streamr-core/api/v1/dataunions/0x6c90aece04198da2d5ca9b956b8f95af8041de37/secrets/L-TvrBkyQTS_JK1ABHFEZAaZ3FHq7-TPqMXe9JNz1x6g
	@StreamrApi
	def show(String contractAddress, String id) {
		checkAdminAccessControl(loggedInUser(), contractAddress)
		if (!isValidID(id)) {
			throw new BadRequestException("Data Union secret id is not valid")
		}
		DataUnionSecret secret = dataUnionSecretService.find(contractAddress, id)
		if (secret == null) {
			throw new NotFoundException("Data Union secret not found by id")
		}
		render(secret.toMap() as JSON)
	}

	// curl -v -X PUT -H "Authorization: token tester1-api-key" -H "Content-Type: application/json" -d '{"name":"new name"}' http://localhost:8081/streamr-core/api/v1/dataunions/0x6c90aece04198da2d5ca9b956b8f95af8041de37/secrets/L-TvrBkyQTS_JK1ABHFEZAaZ3FHq7-TPqMXe9JNz1x6g
	@StreamrApi
	def update(String contractAddress, String id, DataUnionSecretCommand cmd) {
		checkAdminAccessControl(loggedInUser(), contractAddress)
		if (!isValidID(id)) {
			throw new BadRequestException("Data Union secret id is not valid")
		}
		if (cmd.errors.getFieldError("name")) {
			throw new BadRequestException("name in json is not a valid name")
		}
		DataUnionSecret secret = dataUnionSecretService.update(contractAddress, id, cmd)
		render(secret.toMap() as JSON)
	}

	// curl -v -X DELETE -H "Authorization: token tester1-api-key" http://localhost:8081/streamr-core/api/v1/dataunions/0x6c90aece04198da2d5ca9b956b8f95af8041de37/secrets/L-TvrBkyQTS_JK1ABHFEZAaZ3FHq7-TPqMXe9JNz1x6g
	@StreamrApi
	def delete(String contractAddress, String id) {
		checkAdminAccessControl(loggedInUser(), contractAddress)
		if (!isValidID(id)) {
			throw new BadRequestException("Data Union secret id is not valid")
		}
		dataUnionSecretService.delete(contractAddress, id)
		render(status: 204)
	}
}
