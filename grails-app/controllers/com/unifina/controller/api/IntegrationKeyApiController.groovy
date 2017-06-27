package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.domain.security.IntegrationKey
import com.unifina.security.StreamrApi
import com.unifina.service.ApiService
import com.unifina.service.EthereumIntegrationKeyService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class IntegrationKeyApiController {

	def apiService
	def ethereumIntegrationKeyService

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
			//eq("user", request.apiUser)

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
	def save() {
		IntegrationKey key
		if (!request.JSON.name) {
			throw new ApiException(400, "EMPTY_FIELD", "Name can't be empty!")
		}
		if (request.JSON.service as IntegrationKey.Service == IntegrationKey.Service.ETHEREUM) {
			try {
				key = ethereumIntegrationKeyService.createEthereumAccount(request.apiUser, request.JSON.name, (String) request.JSON.json.get("privateKey"))
			} catch (IllegalArgumentException e) {
				throw new ApiException(400, "INVALID_HEX_STRING", e.message)
			}
		} else {
			throw new ApiException(400, 'INVALID_SERVICE', "Invalid service: $request.JSON.service")
		}

		render key.toMap() as JSON
	}

	@StreamrApi
	def delete(String id) {
		IntegrationKey account = IntegrationKey.findByIdAndUser(id, request.apiUser)
		account.delete(flush: true)
		response.status = 204
		render ""
	}
}
