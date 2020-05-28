package com.unifina.controller.api

import com.unifina.domain.security.*
import com.unifina.security.PasswordEncoder
import com.unifina.service.*
import com.unifina.signalpath.messaging.MockMailService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.context.MessageSource
import spock.lang.Specification

@TestFor(AuthApiController)
@Mock([SignupInvite, SignupCodeService, RegistrationCode, SecUser, Key, SecRole, SecUserSecRole, Permission, UserService])
class AuthApiControllerSpec extends Specification {

	String username = "user@invite.to"
	PasswordEncoder passwordEncoder = new UnitTestPasswordEncoder()

	def messageSource = [
	    getMessage: { error, locale ->
			return error.toString()
		}
	]

	void setup() {
		controller.mailService = new MockMailService()
		controller.passwordEncoder = passwordEncoder
		controller.signupCodeService = new SignupCodeService()
		def permissionService = Stub(PermissionService)
		controller.userService = new UserService()
		controller.userService.passwordEncoder = passwordEncoder
		controller.userService.grailsApplication = grailsApplication as GrailsApplication
		controller.userService.permissionService = permissionService as PermissionService
		controller.userService.messageSource = messageSource as MessageSource
		controller.userService.canvasService = Mock(CanvasService)
		controller.userService.streamService = Mock(StreamService)
	}

	void "signup with bad email should return error"() {
		when: "signing up with bad email"
		params.username = 'foo@bad'
		request.method = 'POST'
		controller.signup()
		then: "should return error"
		response.status == 400
		!response.json.success
		response.json.error.find { e -> e.contains("rejected value [foo@bad]") }
	}

	void "signup with email should create and send invite code"() {
		when: "signing up with email"
		params.username = username
		request.method = 'POST'
		controller.signup()
		then: "should create invite code"
		SignupInvite.count() == 1
		SignupInvite.getAll().get(0).sent
		then: "signup email should be sent"
		controller.mailService.mailSent
		controller.mailService.html.contains("complete")
		response.json.username == username
	}

	void "trying to register without invite code should fail"() {
		when: "going to register page without invite code"
		request.method = "POST"
		controller.register()
		then: "should fail"
		response.status == 400
		!response.json.success
		response.json.error.contains("not valid")
	}

	void "reusing a used invite should fail"() {
		when: "going to register page with a used invite code"
		def inv = controller.signupCodeService.create(username)
		inv.sent = true
		inv.used = true
		inv.save()
		params.invite = inv.code
		request.method = 'POST'
		controller.register()
		then: "should fail"
		response.status == 400
		!response.json.success
		response.json.error.contains("not valid")
	}

	void "using an unsent invite should fail"() {
		when: "going to register page with an unsent invite code"
		def inv = controller.signupCodeService.create(username)
		params.invite = inv.code
		request.method = 'POST'
		controller.register()
		then: "should fail"
		response.status == 400
		!response.json.success
		response.json.error.contains("not valid")
	}

	void "submitting registration without accepting ToS should fail"() {
		when: "not accepting ToS"
		def inv = controller.signupCodeService.create(username)
		inv.sent = true
		inv.save()
		params.invite = inv.code
		params.username = username
		params.name = "Name"
		params.password = 'fooBar123!'
		params.password2 = 'fooBar123!'
		request.method = 'POST'
		controller.register()
		then: "should fail"
		response.status == 400
		!response.json.success
		response.json.error.find { it.contains("field 'tosConfirmed': rejected value [null]") }
	}

	void "submitting registration without matching passwords should fail"() {
		when: "submitting unmatched passwords"
		def inv = controller.signupCodeService.create(username)
		inv.sent = true
		inv.save()
		params.invite = inv.code
		params.username = username
		params.name = "Name"
		params.password = 'fooBar123!'
		params.password2 = 'fooBar234!'
		params.tosConfirmed = true
		request.method = 'POST'
		controller.register()
		then: "should fail"
		response.status == 400
		!response.json.success
		response.json.error.find { it.contains("on field 'password2'") }
	}

	void "submitting registration without name should fail"() {
		when: "submitting without name"
		def inv = controller.signupCodeService.create(username)
		inv.sent = true
		inv.save()
		params.invite = inv.code
		params.username = username
		params.password = 'fooBar123!'
		params.password2 = 'fooBar123!'
		params.tosConfirmed = true
		request.method = 'POST'
		controller.register()
		then: "should fail"
		response.status == 400
		!response.json.success
		response.json.error.find { it.contains("on field 'name': rejected value [null]") }
	}

	void "submitting registration with weak password should fail"() {
		when: "weak password"
		def inv = controller.signupCodeService.create(username)
		inv.sent = true
		inv.save()
		params.invite = inv.code
		params.username = username
		params.name = "Name"
		params.password = 'weak'
		params.password2 = 'weak'
		params.tosConfirmed = true
		request.method = 'POST'
		controller.register()
		then: "should fail"
		response.status == 400
		!response.json.success
		response.json.error.find { it.contains("on field 'password'") }
	}

	void "submitting registration for existing username fails with 400 status"() {
		when: "username already exists"
		def user = new SecUser(
			id: 1,
			username: "test@test.com",
			name: "Test User",
			password: passwordEncoder.encodePassword("foobar123!"),
		)
		user.save(validate: false)
		def inv = controller.signupCodeService.create(user.username)
		inv.sent = true
		inv.save()

		params.invite = inv.code
		params.name = "Name"
		params.username = user.username
		params.password = 'fooBar123!'
		params.password2 = 'fooBar123!'
		params.tosConfirmed = true
		request.method = 'POST'
		controller.register()
		then: "should fail"
		response.status == 400
		!response.json.success
		response.json.error == 'User already exists'
	}

	void "submitting registration with valid invite should create user"() {
		setup:
		// The roles created
		["ROLE_USER","ROLE_LIVE","ROLE_ADMIN"].each {
			def role = new SecRole()
			role.authority = it
			role.save()
		}

		when: "registering with valid invite code"
		def inv = controller.signupCodeService.create(username)
		inv.sent = true
		inv.save()
		params.invite = inv.code
		params.name = "Name"
		params.username = username
		params.password = 'fooBar123!'
		params.password2 = 'fooBar123!'
		params.tosConfirmed = true
		request.method = 'POST'
		controller.register()

		then: "should create user"
		SecUser.findByUsername(username)
		SecUser.findByUsername(username).password == 'fooBar123!-encoded'
		response.status == 200
		response.json.name == "Name"
		response.json.username == "user@invite.to"

		then: "welcome email should be sent"
		controller.mailService.mailSent
	}

	void "forgotPassword returns emailSent=true but does not send email if the user does not exist"() {
		EmailCommand cmd = new EmailCommand()

		when: "requested new password"
		cmd.username = "test@streamr.com"
		request.method = "POST"
		controller.forgotPassword(cmd)
		then:
		!controller.mailService.mailSent
		response.json.emailSent
	}

	void "forgotPassword sends email and returns emailSent=true if the user exists"() {
		EmailCommand cmd = new EmailCommand()
		SecUser me = new SecUser()
		me.username = "test@streamr.com"
		me.save(validate: false)

		when: "requested new password"
		cmd.username = "test@streamr.com"
		request.method = "POST"
		controller.forgotPassword(cmd)
		then:
		controller.mailService.mailSent
		// The text of the html contains the link
		controller.mailService.html.contains("/resetPassword?t=")
		response.json.emailSent	}
}
