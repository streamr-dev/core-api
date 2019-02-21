package com.unifina.controller.security

import com.unifina.domain.security.SecUser
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_FULLY"])
class ProfileController {

	def springSecurityService
	def userService

	static defaultAction = "edit"

	static allowedMethods = [
		update: "POST",
		changePwd: "POST",
	]

	def edit() {
		def currentUser = SecUser.get(springSecurityService.currentUser.id)
		[user: currentUser]
	}

	/**
	 * @Deprecated See ProfileApiController.update
	 * 21.02.2019. Remove when new front end is deployed.
	 */
	def update() {
		SecUser user = SecUser.get(springSecurityService.currentUser.id)

		// Only these user fields can be updated!
		user.name = params.name ?: user.name

		user = user.save(failOnError: true)
		if (user.hasErrors()) {
			log.warn("Update failed due to validation errors: " + userService.checkErrors(user.errors.getAllErrors()))
			response.setStatus(400)
		} else {
			return render(user.toMap() as JSON)
		}
	}

	/*
	 * @Deprecated See ProfileApiController.changePwd
	 * 20.02.2019. Remove when new front end is deployed.
	 */
	def changePwd(ChangePasswordCommand cmd) {
		def user = SecUser.get(springSecurityService.currentUser.id)
		if (request.method == 'GET') {
			return [user:user]
		} else {
			if (!cmd.validate()) {
				flash.error = "Password not changed!"
				return render(view: 'changePwd', model: [cmd: cmd, user:user])
			} else {
				user.password = springSecurityService.encodePassword(cmd.password)
				user.save(flush:true, failOnError:true)

				springSecurityService.reauthenticate user.username

				log.info("User $user.username changed password!")

				flash.message = "Password changed!"
				redirect(action:"edit")
			}
		}
	}
}

/**
 * @Deprecated See above!
 */
class ChangePasswordCommand {

	def springSecurityService
	def userService

	String currentpassword
	String password
	String password2

	static constraints = {
		currentpassword validator: {String pwd, ChangePasswordCommand cmd->
			def encoder = cmd.springSecurityService.passwordEncoder
			def encodedPassword = SecUser.get(cmd.springSecurityService.currentUser.id).password
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
