package com.unifina.controller.api


import com.unifina.api.BadRequestException
import com.unifina.api.DataUnionSecretCommand
import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.domain.dataunion.DataUnionSecret
import com.unifina.domain.security.SecUser
import com.unifina.security.StreamrApi
import com.unifina.service.DataUnionSecretService

import com.unifina.service.EthereumService
import com.unifina.utils.EthereumAddressValidator
import grails.converters.JSON

class DataUnionSecretApiController {
	DataUnionSecretService dataUnionSecretService
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

	void checkAdminAccessControl(SecUser user, String contractAddress) {
		if (!EthereumAddressValidator.validate(contractAddress)) {
			throw new BadRequestException("Data Union address is not a valid Ethereum address")
		}
		String adminAddress = ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress)
		if (adminAddress == null) {
			throw new BadRequestException("Data Union address is not of a data union smart contract")
		}
		if (!ethereumService.hasEthereumAddress(user, adminAddress)) {
			throw new NotPermittedException(user?.username, "data union", contractAddress, "manage")
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
