
package com.unifina.controller.security

import com.unifina.domain.data.Feed
import com.unifina.domain.security.*
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.feed.NoOpStreamListener
import com.unifina.service.PermissionService
import com.unifina.service.SignupCodeService
import com.unifina.service.UserService
import com.unifina.signalpath.messaging.MockMailService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(RegisterController)
@Mock([SignupInvite, SignupCodeService, RegistrationCode, SecUser, Key, SecRole, SecUserSecRole,
		Feed, ModulePackage, PermissionService, Permission, UserService])
class RegisterControllerSpec extends Specification {

	def username = "user@invite.to"
	String reauthenticated = null

	void setupSpec() {

	}

	def springSecurityService = [
			encodePassword: { pw ->
				return pw+"-encoded"
			},
			reauthenticate: {username->
				reauthenticated = username
			}
	]

	void setup() {
		controller.mailService = new MockMailService()
		
		controller.springSecurityService = springSecurityService
		controller.signupCodeService = new SignupCodeService()
		def permissionService = new PermissionService()
		permissionService.grailsApplication = grailsApplication
		controller.userService = new UserService()
		controller.userService.springSecurityService = springSecurityService
		controller.userService.grailsApplication = grailsApplication
		controller.userService.permissionService = permissionService
	}

	void "index should not be available"() {
		when: "index is requested"
			controller.index()
		then: "404 is returned"
			response.status == 404
	}

	void "signup with bad email should return error"() {
		when: "signing up with bad email"
			params.username = 'foo@bad'
			request.method = 'POST'
			controller.signup()
		then: "should return error"
			model.user.errors.allErrors.size() > 0
	}

	void "signup with email should create but not send invite code if requireInvite = true"() {
		setup:
			controller.grailsApplication.config.streamr.signup.requireInvite = true
		when: "signing up with email"
			params.username = username
			request.method = 'POST'
			controller.signup()
		then: "should create invite code"
			SignupInvite.count() == 1
			view == '/register/waitForInvitation'
		then: "signup email should be sent"
			controller.mailService.mailSent
			controller.mailService.html.contains("invite")
	}

	void "signup with email should create and send invite code if requireInvite = false"() {
		setup:
			controller.grailsApplication.config.streamr.signup.requireInvite = false
		when: "signing up with email"
			params.username = username
			request.method = 'POST'
			controller.signup()
		then: "should create invite code"
			SignupInvite.count() == 1
			SignupInvite.getAll().get(0).sent
			view == '/register/registerLinkSent'
		then: "signup email should be sent"
			controller.mailService.mailSent
			controller.mailService.html.contains("complete")
	}

	void "sending an invite with nonexistent code should fail"() {
		when: "sending an invite with nonexistent code"
			params.code = "not a valid code"
			controller.sendInvite()
		then: "should error"
			flash.message.contains('not found')
			!controller.mailService.mailSent
			response.redirectedUrl == '/register/list'
	}

	void "sending invite should send mail"() {
		when: "sending an invite to signed up user"
			def inv = controller.signupCodeService.create(username)
			params.code = inv.code
			controller.sendInvite()
		then: "should send mail"
			inv.sent
			controller.mailService.mailSent
			response.redirectedUrl == '/register/list'
	}

	void "trying to register without invite code should fail"() {
		when: "going to register page without invite code"
			controller.register()
		then: "should fail"
			flash.message != null
			response.redirectedUrl == '/register/signup'
	}

	void "reusing a used invite should fail"() {
		when: "going to register page with a used invite code"
			def inv = controller.signupCodeService.create(username)
			inv.sent = true
			inv.used = true
			inv.save()
			params.invite = inv.code
			request.method = 'GET'
			controller.register()
		then: "should fail"
			response.redirectedUrl == '/register/signup'
			flash.message != null
	}

	void "using an unsent invite should fail"() {
		when: "going to register page with an unsent invite code"
			def inv = controller.signupCodeService.create(username)
			params.invite = inv.code
			request.method = 'GET'
			controller.register()
		then: "should fail"
			response.redirectedUrl == '/register/signup'
			flash.message != null
	}

	void "link should show registration page with values from invite"() {
		when: "going to register page with valid invite code"
			def inv = controller.signupCodeService.create(username)
			inv.sent = true
			inv.save()
			params.invite = inv.code
			request.method = 'GET'
			controller.register()
		then: "should show form with values from invite"
			view == '/register/register'
			model.user.username == username
			model.invite == inv.code
	}

	void "submitting registration without accepting ToS should fail"() {
		when: "not accepting ToS"
			def inv = controller.signupCodeService.create(username)
			inv.sent = true
			inv.save()
			params.invite = inv.code
			params.username = username
			params.name = "Name"
			params.pwdStrength = 2
			params.password = 'fooBar123!'
			params.password2 = 'fooBar123!'
			params.timezone = 'NoContinent/NoPlace'
			request.method = 'POST'
			controller.register()
		then: "should fail"
			model.user.errors.allErrors.size() == 1
			view == '/register/register'
	}

	void "submitting registration without matching passwords should fail"() {
		when: "submitting unmatched passwords"
			def inv = controller.signupCodeService.create(username)
			inv.sent = true
			inv.save()
			params.invite = inv.code
			params.username = username
			params.name = "Name"
			params.pwdStrength = 2
			params.password = 'fooBar123!'
			params.password2 = 'fooBar234!'
			params.timezone = 'NoContinent/NoPlace'
			params.tosConfirmed = 'on'
			request.method = 'POST'
			controller.register()
		then: "should fail"
			model.user.errors.allErrors.size() == 1
			view == '/register/register'
	}
	
	void "submitting registration without name should fail"() {
		when: "submitting without name"
			def inv = controller.signupCodeService.create(username)
			inv.sent = true
			inv.save()
			params.invite = inv.code
			params.username = username
			params.pwdStrength = 2
			params.password = 'fooBar123!'
			params.password2 = 'fooBar123!'
			params.timezone = 'NoContinent/NoPlace'
			params.tosConfirmed = 'on'
			request.method = 'POST'
			controller.register()
		then: "should fail"
			model.user.errors.allErrors.size() == 1
			view == '/register/register'
	}
	
	void "submitting registration with weak password should fail"() {
		when: "weak password"
			def inv = controller.signupCodeService.create(username)
			inv.sent = true
			inv.save()
			params.invite = inv.code
			params.username = username
			params.name = "Name"
			params.pwdStrength = 0
			params.password = 'fooBar123!'
			params.password2 = 'fooBar123!'
			params.timezone = 'NoContinent/NoPlace'
			params.tosConfirmed = 'on'
			request.method = 'POST'
			controller.register()
		then: "should fail"
			model.user.errors.allErrors.size() == 1
			view == '/register/register'
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
			feed.streamPageTemplate = ""
			feed.save()

			// A modulePackage created with minimum fields required
			def modulePackage = new ModulePackage()
			modulePackage.id = new Long(1)
			modulePackage.name = "test"
			modulePackage.user = new SecUser()
			modulePackage.save()

			def modulePackage2 = new ModulePackage()
			modulePackage2.id = new Long(2)
			modulePackage2.name = "test2"
			modulePackage2.user = new SecUser()
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
			params.pwdStrength = 2
			params.username = username
			params.password = 'fooBar123!'
			params.password2 = 'fooBar123!'
			params.timezone = 'NoContinent/NoPlace'
			params.tosConfirmed = 'on'
			request.method = 'POST'
			controller.register()
		then: "should create user"
			SecUser.findByUsername(username)
			SecUser.findByUsername(username).timezone == 'NoContinent/NoPlace'
			SecUser.findByUsername(username).password == 'fooBar123!-encoded'
			response.redirectedUrl != null
		then: "welcome email should be sent"
			controller.mailService.mailSent
		then: "user must be (re)authenticated"
			reauthenticated == username
	}

	void "forgotPassword returns emailSent=true but does not send email if the user does not exist"() {
		EmailCommand cmd = new EmailCommand()

		when: "requested new password"
		cmd.username = "test@streamr.com"
		request.method = "POST"
		def model = controller.forgotPassword(cmd)
		then:
		!controller.mailService.mailSent
		model.emailSent
	}

	void "forgotPassword sends email and returns emailSent=true if the user exists"() {
		EmailCommand cmd = new EmailCommand()
		SecUser me = new SecUser()
		me.username = "test@streamr.com"
		me.save(validate: false)

		when: "requested new password"
		cmd.username = "test@streamr.com"
		request.method = "POST"
		def model = controller.forgotPassword(cmd)
		then:
		controller.mailService.mailSent
		// The text of the html contains the link
		controller.mailService.html.contains("register/resetPassword")
		model.emailSent
	}


}
