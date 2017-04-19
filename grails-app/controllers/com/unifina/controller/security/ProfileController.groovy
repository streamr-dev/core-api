package com.unifina.controller.security

import com.unifina.domain.data.Stream
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.service.StreamService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_FULLY"])
class ProfileController {

	def grailsApplication
	def springSecurityService
	def userService
	def permissionService
	def streamService
	
	static defaultAction = "edit"

	def edit() {
		def currentUser = SecUser.get(springSecurityService.currentUser.id)
		[user: currentUser]
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
		List<Key> oldKeys = Key.findAllByUser(user)

		// Revoke old keys
		Stream revokeNotificationStream = new Stream()
		revokeNotificationStream.id = grailsApplication.config.streamr.apiKey.revokeNotificationStream
		for (Key oldKey : oldKeys) {
			oldKey.delete()
			streamService.sendMessage(revokeNotificationStream, [
				action: "revoked",
				user: user.id,
				key: oldKey.id
			], 60)
		}

		new Key(name: 'Key for ' + user.username, user: user).save(validate: true, failOnError: true, flush: true)
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