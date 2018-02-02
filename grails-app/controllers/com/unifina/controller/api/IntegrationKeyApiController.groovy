package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.domain.security.IntegrationKey
import com.unifina.security.StreamrApi
import com.unifina.service.ApiService
import com.unifina.service.EthereumIntegrationKeyService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonSlurper

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class IntegrationKeyApiController {

	ApiService apiService
	EthereumIntegrationKeyService ethereumIntegrationKeyService

	@StreamrApi
	def index() {
		if (params.service) {
			try {
				params.service as IntegrationKey.Service
			} catch (IllegalArgumentException e) {
				throw new ApiException(400, "INVALID_SERVICE", "Invalid service: $request.JSON.service")
			}
		}
		def criteria = apiService.createListCriteria(params, ["id"], {
			eq "user", request.apiUser

			// Filter by exact id
			if (params.id) {
				eq "id", params.id
			}
			// Filter by exact service
			if (params.service) {
				eq "service", params.service
			}
		})
		def integrationKeys = IntegrationKey.findAll(criteria)
		render integrationKeys*.toMap() as JSON
	}

	@StreamrApi
	def save(SaveIntegrationKeyCommand cmd) {
		if (cmd.service as IntegrationKey.Service == IntegrationKey.Service.ETHEREUM) {
			IntegrationKey key
			try {
				key = ethereumIntegrationKeyService.createEthereumAccount(request.apiUser, cmd.name, (String) request.JSON.json.get("privateKey"))
			} catch (IllegalArgumentException e) {
				throw new ApiException(400, "INVALID_HEX_STRING", e.message)
			}
			render key.toMap() as JSON
		} else if (cmd.service as IntegrationKey.Service == IntegrationKey.Service.ETHEREUM_ID) {
			IntegrationKey key = ethereumIntegrationKeyService.createEthereumID(request.apiUser, cmd.name, cmd.challenge.id, cmd.challenge.challenge, cmd.signature)
			response.status = 201
			Map json = new JsonSlurper().parseText(key.json)
			render([
					name     : cmd.name,
					challenge: [
							id       : cmd.challenge.id,
							challenge: cmd.challenge.challenge
					],
					signature: cmd.signature,
					address  : json.address
			] as JSON)
		} else {
			throw new ApiException(400, 'INVALID_SERVICE', "Invalid service: $request.JSON.service")
		}
	}

	@StreamrApi
	def delete(String id) {
		IntegrationKey account = IntegrationKey.findByIdAndUser(id, request.apiUser)
		account.delete(flush: true)
		response.status = 204
		render ""
	}
}
