package com.unifina.controller.security

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import com.unifina.domain.security.SecUser

@Secured(["IS_AUTHENTICATED_FULLY"])
class ProfileController {
	
	def springSecurityService
	def userService
	def kafkaService
	
	static defaultAction = "edit"

	def edit() {
		[user:SecUser.get(springSecurityService.currentUser.id)]
	}
	
	def update() {
		SecUser user = SecUser.get(springSecurityService.currentUser.id)
		user.properties = params
		user.name = params.name // for some reason not updated by above row
		
		if (!user.save()) {
			flash.error = "Profile not updated!"
			log.warn("Update failed due to validation errors: "+userService.checkErrors(user.errors.getAllErrors()))
			return render(view: 'edit', model: [user: user])
		}
		else {
			flash.message = "Profile updated."
			redirect(action:"edit")
		}
	}

	def regenerateApiKey() {
		SecUser user = SecUser.get(springSecurityService.currentUser.id)
		String oldApiKey = user.apiKey
		user.apiKey = user.generateApiKey()
		kafkaService.sendMessage("revoked-api-keys", "", [
		        action: "revoked",
				user: user.id,
				key: oldApiKey
		])
		log.info("User $user.username regenerated api key!")
		render ([success: true] as JSON)
	}
	
	def changePwd(ChangePasswordCommand cmd) {
		def user = SecUser.get(springSecurityService.currentUser.id)
		if (request.method == 'GET') {
			return [user:user]
		}
		else {
			if (!cmd.validate()) {
				flash.error = "Password not changed!"
				return render(view: 'changePwd', model: [cmd: cmd, user:user])
			}
			else {
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

class ChangePasswordCommand {
	
	def springSecurityService
	def userService
	
	String currentpassword
	String password
	String password2
	Integer pwdStrength

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