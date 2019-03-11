package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.domain.security.SecUser
import com.unifina.security.StreamrApi
import com.unifina.service.UserService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.Validateable

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class UserApiController {
	static allowedMethods = [
		update: "PUT",
		changePassword: "POST",
		getUserInfo: "GET",
		delete: "DELETE",
	]

	def springSecurityService
	UserService userService

	@StreamrApi
	def update(UpdateProfileCommand cmd) {
		SecUser user = loggedInUser()
		// Only these user fields can be updated!
		user.name = cmd.name ?: user.name
		user = user.save(failOnError: true)
		if (user.hasErrors()) {
			log.warn("Update failed due to validation errors: " + userService.checkErrors(user.errors.getAllErrors()))
			throw new ApiException(400, "PROFILE_UPDATE_FAILED", "Profile update failed.")
		}
		return render(user.toMap() as JSON)
	}

	@StreamrApi
	def changePassword(ChangePasswordCommand cmd) {
		if (!cmd.validate()) {
			throw new ApiException(400, "PASSWORD_CHANGE_FAILED", "Password not changed!")
		}
		SecUser user = loggedInUser()
		user.password = springSecurityService.encodePassword(cmd.password)
		user.save(flush: true, failOnError: true)
		log.info("User $user.username changed password!")
		render(status: 204, body: "")
	}

	SecUser loggedInUser() {
		return (SecUser) request.apiUser
	}

	@StreamrApi
	def getUserInfo() {
		render(request.apiUser?.toMap() as JSON)
	}

	@StreamrApi
	def delete() {
		SecUser user = (SecUser) request.apiUser
		userService.delete(user)
		render(status: 204, "")
	}
}

@Validateable
class UpdateProfileCommand {
	String name
}

@Validateable
class ChangePasswordCommand {

	def springSecurityService
	def userService

	String username
	String currentpassword
	String password
	String password2

	static constraints = {
		username(blank: false)
		currentpassword validator: {String pwd, ChangePasswordCommand cmd->
			def user = cmd.userService.getUserFromUsernameAndPassword(cmd.username, cmd.currentpassword)
			if (user == null) {
				return false
			}
			def encodedPassword = user.password
			def encoder = cmd.springSecurityService.passwordEncoder
			return encoder.isPasswordValid(encodedPassword, pwd, null /*salt*/)
		}
		password validator: {String password, ChangePasswordCommand command ->
			return command.userService.passwordValidator(password, command)
		}
		password2 validator: {value, ChangePasswordCommand command ->
			return command.userService.password2Validator(value, command)
		}
	}
}
