package com.unifina.controller.security

import com.unifina.domain.data.Feed
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.RegistrationCode
import com.unifina.domain.security.SecRole
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SecUserSecRole
import com.unifina.domain.security.SignupInvite
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.feed.NoOpStreamListener
import com.unifina.service.PermissionService
import com.unifina.service.SignupCodeService
import com.unifina.service.UserService
import com.unifina.signalpath.messaging.MockMailService
import grails.plugin.springsecurity.SpringSecurityService
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.context.MessageSource
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(AuthController)
@Mock([SignupInvite, SignupCodeService, RegistrationCode, SecUser, Key, SecRole, SecUserSecRole, Feed, ModulePackage, Permission, UserService])
class AuthControllerSpec extends Specification {

	String username = "me@invite.to"
	String reauthenticated = null

	void setupSpec() {

	}

	def springSecurityService = [
		encodePassword: { pw ->
			return pw + "-encoded"
		},
		reauthenticate: { username->
			reauthenticated = username
		}
	]

	def messageSource = [
	    getMessage: { error, locale ->
			return error.toString()
		}
	]

	void setup() {
		controller.mailService = new MockMailService()
		controller.springSecurityService = springSecurityService as SpringSecurityService
		controller.signupCodeService = new SignupCodeService()
		def permissionService = Stub(PermissionService)
		controller.userService = new UserService()
		controller.userService.springSecurityService = springSecurityService as SpringSecurityService
		controller.userService.grailsApplication = grailsApplication as GrailsApplication
		controller.userService.permissionService = permissionService as PermissionService
		controller.userService.messageSource = messageSource as MessageSource
		reauthenticated = null
	}

	void "index should be available"() {
		when: "index is requested"
		controller.index()
		then: "200 is returned"
		response.status == 200
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
		params.timezone = 'NoContinent/NoPlace'
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
		params.timezone = 'NoContinent/NoPlace'
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
		params.timezone = 'NoContinent/NoPlace'
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
		params.timezone = 'NoContinent/NoPlace'
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
			password: springSecurityService.encodePassword("foobar123!"),
			timezone: "Europe/Helsinki"
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
		params.timezone = 'NoContinent/NoPlace'
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
		// A feed created with minimum fields required
		Feed feed = new Feed()
		feed.id = new Long(7)
		feed.name = "testFeed"
		feed.eventRecipientClass = ""
		feed.keyProviderClass = ""
		feed.messageSourceClass = ""
		feed.module = new Module()
		feed.parserClass = ""
		feed.timezone = "Europe/Minsk"
		feed.streamListenerClass = NoOpStreamListener.name
		feed.save()

		// A modulePackage created with minimum fields required
		def modulePackage = new ModulePackage()
		modulePackage.id = new Long(1)
		modulePackage.name = "test"
		modulePackage.save()

		def modulePackage2 = new ModulePackage()
		modulePackage2.id = new Long(2)
		modulePackage2.name = "test2"
		modulePackage2.save()

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
		params.timezone = 'NoContinent/NoPlace'
		params.tosConfirmed = true
		request.method = 'POST'
		controller.register()
		then: "should create me"
		SecUser.findByUsername(username)
		SecUser.findByUsername(username).timezone == 'NoContinent/NoPlace'
		SecUser.findByUsername(username).password == 'fooBar123!-encoded'
		response.status == 200
		response.json == [
		    timezone: "NoContinent/NoPlace",
			name: "Name",
			username: "me@invite.to"
		]
		then: "welcome email should be sent"
		controller.mailService.mailSent
		then: "me must be (re)authenticated"
		reauthenticated == username
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
		controller.mailService.html.contains("register/resetPassword")
		response.json.emailSent	}
}
