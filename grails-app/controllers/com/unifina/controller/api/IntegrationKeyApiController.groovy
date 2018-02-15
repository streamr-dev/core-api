package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.api.IntegrationKeyListParams
import com.unifina.domain.security.IntegrationKey
import com.unifina.security.StreamrApi
import com.unifina.service.ApiService
import com.unifina.service.EthereumIntegrationKeyService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonSlurper
import org.grails.datastore.mapping.query.api.BuildableCriteria

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class IntegrationKeyApiController {
	EthereumIntegrationKeyService ethereumIntegrationKeyService

	@StreamrApi
	def index(IntegrationKeyListParams listParams) {
		def criteria = listParams.createListCriteria() << {
			eq("user", request.apiUser)
		}
		def integrationKeys = IntegrationKey.withCriteria(criteria)
		render(integrationKeys*.toMap() as JSON)
	}

	@StreamrApi
	def save(SaveIntegrationKeyCommand cmd) {
		if (cmd.service as IntegrationKey.Service == IntegrationKey.Service.ETHEREUM) {
			IntegrationKey key
			try {
				key = ethereumIntegrationKeyService.createEthereumAccount(request.apiUser, cmd.name, cmd.json.privateKey)
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
