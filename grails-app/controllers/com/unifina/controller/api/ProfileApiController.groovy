package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.domain.security.SecUser
import com.unifina.security.StreamrApi
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.Validateable

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class ProfileApiController {
	def springSecurityService

	static allowedMethods = [
		changePwd: "POST",
	]

	@StreamrApi
	def changePwd(ChangePasswordCommand cmd) {
		if (!cmd.validate()) {
			throw new ApiException(400, "PASSWORD_CHANGE_FAILED", "Password not changed!")
		}
		SecUser user = loggedInUser()
		user.password = springSecurityService.encodePassword(cmd.password)
		user.save(flush: true, failOnError: true)
		springSecurityService.reauthenticate(user.username)
		log.info("User $user.username changed password!")
		render(status: 204, body: "")
	}

	SecUser loggedInUser() {
		return (SecUser) request.apiUser
	}
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
