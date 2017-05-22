package com.unifina.controller.api

import com.unifina.domain.security.Account
import com.unifina.security.StreamrApi
import com.unifina.service.ApiService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class AccountApiController {

	ApiService apiService

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
		Account account = new Account()
		account.setName(request.JSON.name)
		account.setJson(request.JSON.json.toString())
		account.user = request.apiUser
		account.type = Account.Type.fromString(request.JSON.type)
		account.save(flush: true, failOnError: true)
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
