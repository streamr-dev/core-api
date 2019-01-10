package com.unifina.controller.security

import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.service.StreamService
import com.unifina.service.UserService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(ProfileController)
@Mock([Key, SecUser])
class ProfileControllerSpec extends Specification {

	SecUser user
	String reauthenticated = null

	def springSecurityService = [
		encodePassword: { pw ->
			return pw+"-encoded"
		},
		passwordEncoder: [
			isPasswordValid: {encodedPassword, rawPwd, salt->
				return rawPwd+"-encoded" == encodedPassword
			}
		],
		reauthenticate: {username->
			reauthenticated = username
		}
	]

	void setup() {
		controller.springSecurityService = springSecurityService
		controller.streamService = Mock(StreamService)
		user = new SecUser(id:1,
			username:"test@test.com",
			name: "Test User",
			password:springSecurityService.encodePassword("foobar123!"),
			enabled: true,
			lastLogin: new Date(),
		)
		user.save(validate:false)
		springSecurityService.currentUser = user

		assert SecUser.count()==1
	}

	void "submitting valid content in user password change form must change user password"() {
		when: "password change form is submitted"
			def cmd = new ChangePasswordCommand(currentpassword: "foobar123!", password: 'barbar123!', password2: 'barbar123!', pwdStrength: 3)
			cmd.springSecurityService = springSecurityService
			cmd.userService = new UserService()
			request.method = 'POST'
			controller.changePwd(cmd)
		then: "password must be changed"
			springSecurityService.passwordEncoder.isPasswordValid(SecUser.get(1).password, 'barbar123!', null)
		then: "user must be reauthenticated"
			reauthenticated == user.username
		then: "must redirect and show success message"
			flash.error == null
			response.redirectedUrl == '/profile/edit'
			flash.message.contains("changed")
	}

	void "submitting an invalid current password won't let the password be changed"() {
		when: "password change form is submitted with invalid password"
			def cmd = new ChangePasswordCommand(currentpassword: "invalid", password: 'barbar123!', password2: 'barbar123!')
			cmd.springSecurityService = springSecurityService
			cmd.userService = new UserService()
			request.method = 'POST'
			controller.changePwd(cmd)
		then: "the old password must remain valid"
			springSecurityService.passwordEncoder.isPasswordValid(SecUser.get(1).password, 'foobar123!', null)
		then: "user must not be reauthenticated"
			!reauthenticated
		then: "must stay on page and show error message"
			flash.error != null
			view == '/profile/changePwd'
	}

	void "submitting a too short new password won't let the password be changed"() {
		when: "password change form is submitted with invalid new password"
			def cmd = new ChangePasswordCommand(currentpassword: "foobar", password: 'asd', password2: 'asd', pwdStrength: 0)
			cmd.springSecurityService = springSecurityService
			cmd.userService = new UserService()
			request.method = 'POST'
			controller.changePwd(cmd)
		then: "the old password must remain valid"
			springSecurityService.passwordEncoder.isPasswordValid(SecUser.get(1).password, 'foobar123!', null)
		then: "user must not be reauthenticated"
			!reauthenticated
		then: "must stay on page and show error message"
			flash.error != null
			view == '/profile/changePwd'
	}

	void "submitting a too weak new password won't let the password be changed"() {
		when: "password change form is submitted with invalid new password"
			def cmd = new ChangePasswordCommand(currentpassword: "foobar123", password: 'asd', password2: 'asd', pwdStrength: 0)
			cmd.springSecurityService = springSecurityService
			cmd.userService = new UserService()
			request.method = 'POST'
			controller.changePwd(cmd)
		then: "the old password must remain valid"
			springSecurityService.passwordEncoder.isPasswordValid(SecUser.get(1).password, 'foobar123!', null)
		then: "user must not be reauthenticated"
			!reauthenticated
		then: "must stay on page and show error message"
			flash.error != null
			view == '/profile/changePwd'
	}

	void "changing user settings must change them"() {
		controller.userService = new UserService()
		controller.userService.grailsApplication = grailsApplication
		when: "new settings are submitted"
			request.method = 'POST'
			params.name = "Changed Name"
			controller.update()
		then: "values must be updated and show update message"
			SecUser.get(1).name == "Changed Name"
			response.json.name == "Changed Name"
	}

	void "sensitive fields cannot be changed"() {
		controller.userService = new UserService()
		controller.userService.grailsApplication = grailsApplication

		when:
		request.method = 'POST'
		params.username = "attacker@email.com"
		params.enabled = false
		controller.update()

		then:
		SecUser.get(1).username == "test@test.com"
		response.json.username == "test@test.com"
		SecUser.get(1).enabled
	}
}
