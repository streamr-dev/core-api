package com.unifina.controller.api

import com.unifina.domain.security.RegistrationCode
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SignupInvite
import com.unifina.exceptions.UserCreationFailedException
import com.unifina.security.AuthLevel
import com.unifina.security.PasswordEncoder
import com.unifina.security.StreamrApi
import com.unifina.service.SignupCodeService
import com.unifina.service.UserService
import com.unifina.utils.EmailValidator
import grails.converters.JSON

class AuthApiController {

	def mailService
	UserService userService
	SignupCodeService signupCodeService
	PasswordEncoder passwordEncoder

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def signup(EmailCommand cmd) {
		if (cmd.hasErrors()) {
			response.status = 400
			return render([success: false, error: userService.beautifyErrors(cmd.errors.getAllErrors())] as JSON)
		}

		def existingUser = SecUser.findByUsername(cmd.username)
		if (existingUser) {
			response.status = 400
			return render([success: false, error: "User exists already"] as JSON)
		}

		SignupInvite invite = signupCodeService.create(cmd.username)

		invite.sent = true
		if (!invite.save(flush: false)) {
			log.warn("Failed to save invite: ${invite.errors}")
			response.status = 500
			return render([success: false, error: "Failed to save invite: ${invite.errors}"] as JSON)
		}

		log.info("Signed up $invite.username")

		try {
			mailService.sendMail {
				from grailsApplication.config.unifina.email.sender
				to invite.username
				subject grailsApplication.config.unifina.email.registerLink.subject
				html g.render(template: "/emails/email_register_link", model: [user: invite])
			}
		} catch (Exception error) {
			log.warn("Sending email failed, inviteId: ${invite.id}, error: ${error.getMessage()}")
			response.status = 500
			return render([success: false, error: "Sending email failed"] as JSON)
		}

		return render([username: cmd.username] as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def register(RegisterCommand cmd) {
		SignupInvite invite = SignupInvite.findByCode(cmd.invite)
		if (!invite || invite.used || !invite.sent) {
			response.status = 400
			return render([success: false, error: "Invitation code not valid"] as JSON)
		}

		log.info("Activated invite for ${invite.username}, ${invite.code}")

		def user

		if (cmd.hasErrors()) {
			log.warn("Registration command has errors: ${userService.checkErrors(cmd.errors.getAllErrors())}")
			response.status = 400
			return render([success: false, error: userService.beautifyErrors(cmd.errors.getAllErrors())] as JSON)
		}

		if (SecUser.findByUsername(invite.username)) {
			response.status = 400
			return render([success: false, error: "User already exists"] as JSON)
		}

		try {
			user = userService.createUser([:] << cmd.properties << [username: invite.username])
		} catch (UserCreationFailedException e) {
			response.status = 500
			return render([success: false, error: e.getMessage()] as JSON)
		}

		invite.used = true
		if (!invite.save(flush: false)) {
			log.warn("Failed to save invite: ${invite.errors}")
			response.status = 500
			return render([success: false, error: "Failed to save invite: ${invite.errors}"] as JSON)
		}

		try {
			mailService.sendMail {
				from grailsApplication.config.unifina.email.sender
				to user.username
				subject grailsApplication.config.unifina.email.welcome.subject
				html g.render(template: "/emails/email_welcome", model: [user: user])
			}
		} catch (Exception error) {
			log.warn("Sending email failed, userId: ${user.id}, error: ${error.getMessage()}")
			response.status = 500
			return render([success: false, error: "Sending email failed"] as JSON)
		}

		render(user.toMap() as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def forgotPassword(EmailCommand cmd) {
		if (cmd.hasErrors()) {
			response.status = 400
			return render([success: false, error: userService.beautifyErrors(cmd.errors.getAllErrors())] as JSON)
		}

		def user = SecUser.findWhere(username: cmd.username)
		if (!user) {
			return render([emailSent: true] as JSON) // don't reveal users
		}

		def registrationCode = new RegistrationCode(username: user.username)
		registrationCode.save(flush: false)

		try {
			mailService.sendMail {
				from grailsApplication.config.unifina.email.sender
				to user.username
				subject grailsApplication.config.unifina.email.forgotPassword.subject
				html g.render(template: "/emails/email_forgot_password", model: [token: registrationCode.token])
			}
		} catch (Exception error) {
			log.warn("Sending email failed, userId: ${user.id}, error: ${error.getMessage()}")
			response.status = 500
			return render([success: false, error: "Sending email failed"] as JSON)
		}

		return render([emailSent: true] as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def resetPassword(ResetPasswordCommand command) {

		String token = params.t

		def registrationCode = token ? RegistrationCode.findByToken(token) : null

		if (!registrationCode) {
			response.status = 422
			return render([success: false, error: message(code: 'spring.security.ui.resetPassword.badCode')] as JSON)
		}

		def user = SecUser.findByUsername(registrationCode.username)
		if (!user)
			throw new RuntimeException("User belonging to the registration code was not found: $registrationCode.username")

		command.username = registrationCode.username
		command.validate()

		if (command.hasErrors()) {
			return render([success: false, error: userService.beautifyErrors(command.errors.getAllErrors())] as JSON)
		}

		RegistrationCode.withTransaction { status ->
			user.password = passwordEncoder.encodePassword(command.password)
			user.save()
			registrationCode.delete()
		}

		return render(user.toMap() as JSON)
	}
}

class EmailCommand {
	String username
	static constraints = {
		username blank: false, validator: EmailValidator.validate
	}
}

class RegisterCommand {
	String invite
	String name
	String password
	String password2
	String tosConfirmed

	UserService userService

	static constraints = {
		invite blank: false
		tosConfirmed blank: false, validator: { val -> new Boolean(val) }
		name blank: false
		password validator: { String password, RegisterCommand command ->
			return command.userService.passwordValidator(password, command)
		}
		password2 validator: { String password2, RegisterCommand command ->
			return command.userService.password2Validator(password2, command)
		}
	}
}

class ResetPasswordCommand {
	String username
	String password
	String password2

	UserService userService

	static constraints = {
		username blank: false
		password validator: { String password, ResetPasswordCommand command ->
			return command.userService.passwordValidator(password, command)
		}
		password2 validator: { value, ResetPasswordCommand command ->
			return command.userService.password2Validator(value, command)
		}
	}
}
