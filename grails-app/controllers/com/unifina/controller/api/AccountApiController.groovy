package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.domain.security.Account
import com.unifina.security.StreamrApi
import com.unifina.service.ApiService
import com.unifina.service.EthereumAccountService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class AccountApiController {

	ApiService apiService
	EthereumAccountService ethereumAccountService

	@StreamrApi
	def index() {
		def criteria = apiService.createListCriteria(params, ["id"], {
			// Filter by exact name
			if (params.id) {
				eq "id", params.id
			}
		})
		def accounts = Account.findAll(criteria)
		render accounts*.toMap() as JSON
	}

	@StreamrApi
	def save() {
		Account account
		if (!request.JSON.name) {
			throw new ApiException(400, "EMPTY_FIELD", "Name can't be empty!")
		}
		if (Account.Type.fromString(request.JSON.type) == Account.Type.ETHEREUM) {
			try {
				account = ethereumAccountService.createEthereumAccount(request.apiUser, request.JSON.name, request.JSON.json)
			} catch (IllegalArgumentException e) {
				throw new ApiException(400, "INVALID_HEX_STRING", e.message)
			}
		} else {
			response.status = 400
			return render ([
			        error: "Invalid type: $request.JSON.type"
			]) as JSON
		}

		render account.toMap() as JSON
	}

	@StreamrApi
	def delete(String id) {
		Account account = Account.findById(id)
		account.delete(flush: true)
		response.status = 204
		render ""
	}
}
