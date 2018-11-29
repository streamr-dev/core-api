package com.unifina.controller.api

import com.unifina.api.CreateUserCommand
import com.unifina.api.ValidationException
import com.unifina.domain.security.SecUser
import com.unifina.security.AllowRole
import com.unifina.security.StreamrApi
import com.unifina.service.UserService
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class UserApiController {

	UserService userService

	@StreamrApi
	def getUserInfo() {
		render(request.apiUser?.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(allowRoles = AllowRole.ADMIN)
	def save(CreateUserCommand createUserCommand) {
		if (!createUserCommand.validate()) {
			throw new ValidationException(createUserCommand.errors)
		}
		SecUser user = (SecUser) userService.createUser(createUserCommand.properties)
		render(user.toMap() as JSON)
	}
}
