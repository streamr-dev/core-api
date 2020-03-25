package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.api.IntegrationKeyListParams
import com.unifina.api.NotFoundException
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import com.unifina.security.StreamrApi
import com.unifina.service.EthereumIntegrationKeyService
import grails.converters.JSON
import groovy.json.JsonSlurper

class IntegrationKeyApiController {
	EthereumIntegrationKeyService ethereumIntegrationKeyService

	@StreamrApi
	def index(IntegrationKeyListParams listParams) {
		def criteria = listParams.createListCriteria() << {
			eq("user", apiUser())
		}
		def integrationKeys = IntegrationKey.withCriteria(criteria)
		render(integrationKeys*.toMap() as JSON)
	}

	@StreamrApi
	def save(SaveIntegrationKeyCommand cmd) {
		switch (cmd.service as IntegrationKey.Service) {
			case IntegrationKey.Service.ETHEREUM:
				IntegrationKey key
				try {
					key = ethereumIntegrationKeyService.createEthereumAccount(apiUser(), cmd.name, cmd.json.privateKey)
				} catch (IllegalArgumentException e) {
					throw new ApiException(400, "INVALID_HEX_STRING", e.message)
				}
				render key.toMap() as JSON
				break
			case IntegrationKey.Service.ETHEREUM_ID:
				IntegrationKey key = ethereumIntegrationKeyService.createEthereumID(apiUser(), cmd.name, cmd.challenge.id, cmd.challenge.challenge, cmd.signature)
				response.status = 201
				Map json = new JsonSlurper().parseText(key.json)
				render([
					id       : key.id,
					challenge: [
						id       : cmd.challenge.id,
						challenge: cmd.challenge.challenge
					],
					json: [
						address: json.address
					],
					name     : cmd.name,
					service  : IntegrationKey.Service.ETHEREUM_ID.toString(),
					signature: cmd.signature
				] as JSON)
				break
			default:
				throw new ApiException(400, 'INVALID_SERVICE', "Invalid service: $request.JSON.service")
		}
	}

	@StreamrApi
	def delete(String id) {
		ethereumIntegrationKeyService.delete(id, apiUser())
		render(status: 204, body: "")
	}

	@StreamrApi
	def update(String id) {
		Map json = new JsonSlurper().parseText((String) request.JSON)
		if (json.name == null || json.name.trim() == "") {
			throw new ApiException(400, "BODY_NOT_VALID", "must send JSON body containing property 'name'.")
		}
		String name = (String) json.name

		try {
			ethereumIntegrationKeyService.updateKey(apiUser(), id, name)
		} catch (NotFoundException e) {
			throw e
		}
		render(status: 204, body: "")
	}

	SecUser apiUser() {
		request.apiUser
	}
}
