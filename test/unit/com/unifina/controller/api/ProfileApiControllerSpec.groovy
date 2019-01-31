package com.unifina.controller.api

import com.unifina.ControllerSpecification
import com.unifina.api.ApiException
import com.unifina.api.InvalidUsernameAndPasswordException
import com.unifina.domain.security.SecUser
import com.unifina.service.UserService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(ProfileApiController)
@Mock(SecUser)
class ProfileApiControllerSpec extends ControllerSpecification {

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

	def setup() {
		controller.springSecurityService = springSecurityService
		user = new SecUser(id:1,
			username:"test@test.com",
			name: "Test User",
			password:springSecurityService.encodePassword("foobar123!"),
			enabled: true,
		)
		user.save(validate: false)
	}

	void "submitting valid content in user password change form must change user password"() {
		when: "password change form is submitted"
		def cmd = new ChangePasswordCommand(username: user.username, currentpassword: "foobar123!", password: "barbar123!", password2: "barbar123!")
		cmd.springSecurityService = springSecurityService
		cmd.userService = new UserService() {
			SecUser getUserFromUsernameAndPassword(String username, String password) throws InvalidUsernameAndPasswordException {
				return user
			}
		}
		request.method = "POST"
		authenticatedAs(user) {
			controller.changePwd(cmd)
		}
		then: "password must be changed"
		springSecurityService.passwordEncoder.isPasswordValid(SecUser.get(1).password, "barbar123!", null)
		then: "user must be reauthenticated"
		reauthenticated == user.username
		then:
		response.status == 204
	}


	void "submitting an invalid current password won't let the password be changed"() {
		when: "password change form is submitted with invalid password"
		def cmd = new ChangePasswordCommand(username: user.username, currentpassword: "invalid", password: "barbar123!", password2: "barbar123!")
		cmd.springSecurityService = springSecurityService
		cmd.userService = new UserService() {
			SecUser getUserFromUsernameAndPassword(String username, String password) throws InvalidUsernameAndPasswordException {
				return user
			}
		}
		request.method = "POST"
		authenticatedAs(user) {
			controller.changePwd(cmd)
		}
		then: "the old password must remain valid"
		springSecurityService.passwordEncoder.isPasswordValid(SecUser.get(1).password, "foobar123!", null)
		then: "user must not be reauthenticated"
		!reauthenticated
		then:
		def e = thrown(ApiException)
		e.message == "Password not changed!"
		e.code == "PASSWORD_CHANGE_FAILED"
		e.statusCode == 400
	}

	void "submitting a too short new password won't let the password be changed"() {
		when: "password change form is submitted with invalid new password"
		def cmd = new ChangePasswordCommand(username: user.username, currentpassword: "foobar", password: "asd", password2: "asd")
		cmd.springSecurityService = springSecurityService
		cmd.userService = new UserService() {
			SecUser getUserFromUsernameAndPassword(String username, String password) throws InvalidUsernameAndPasswordException {
				return user
			}
		}
		request.method = "POST"
		authenticatedAs(user) {
			controller.changePwd(cmd)
		}
		then: "the old password must remain valid"
		springSecurityService.passwordEncoder.isPasswordValid(SecUser.get(1).password, "foobar123!", null)
		then: "user must not be reauthenticated"
		!reauthenticated
		then:
		def e = thrown(ApiException)
		e.message == "Password not changed!"
		e.code == "PASSWORD_CHANGE_FAILED"
		e.statusCode == 400
	}

	void "submitting a too weak new password won't let the password be changed"() {
		when: "password change form is submitted with invalid new password"
		def cmd = new ChangePasswordCommand(currentpassword: "foobar123", password: "asd", password2: "asd")
		cmd.springSecurityService = springSecurityService
		cmd.userService = new UserService() {
			SecUser getUserFromUsernameAndPassword(String username, String password) throws InvalidUsernameAndPasswordException {
				return user
			}
		}
		request.method = "POST"
		authenticatedAs(user) {
			controller.changePwd(cmd)
		}
		then: "the old password must remain valid"
		springSecurityService.passwordEncoder.isPasswordValid(SecUser.get(1).password, "foobar123!", null)
		then: "user must not be reauthenticated"
		!reauthenticated
		then:
		def e = thrown(ApiException)
		e.message == "Password not changed!"
		e.code == "PASSWORD_CHANGE_FAILED"
		e.statusCode == 400
	}
}
