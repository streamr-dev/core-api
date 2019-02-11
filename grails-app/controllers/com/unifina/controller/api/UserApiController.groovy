package com.unifina.controller.api

import com.unifina.domain.security.SecUser
import com.unifina.security.StreamrApi
import com.unifina.service.UserService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class UserApiController {
	static allowedMethods = [
		getUserInfo: "GET",
		delete: "DELETE",
	]

	UserService userService

	@StreamrApi
	def getUserInfo() {
		render(request.apiUser?.toMap() as JSON)
	}

	@StreamrApi
	def delete(Long id) {
		SecUser user = (SecUser) request.apiUser
		userService.delete(user, id)
		render(status: 204, "")
	}
}
