package com.unifina.controller.security

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

import com.unifina.domain.security.SecUser
import com.unifina.service.UserService

@TestFor(ProfileController)
@Mock([SecUser])
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
		user = new SecUser(id:1, 
			username:"test@test.test", 
			name: "Test User",
			password:springSecurityService.encodePassword("foobar123!"), 
			timezone: "Europe/Helsinki")
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
		when: "new settings are submitted"
			params.name = "Changed Name"
			params.timezone = "Europe/Helsinki"
			controller.update()
		then: "values must be updated and show update message"
			SecUser.get(1).name == "Changed Name"
			SecUser.get(1).timezone == "Europe/Helsinki"
			flash.message != null
	}
	
}
