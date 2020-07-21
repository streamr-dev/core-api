package com.unifina.controller.api

import com.unifina.ControllerSpecification
import com.unifina.api.ApiException
import com.unifina.api.InvalidUsernameAndPasswordException
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.security.PasswordEncoder
import com.unifina.service.UserAvatarImageService
import com.unifina.service.UserService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.springframework.mock.web.MockMultipartFile

@TestFor(UserApiController)
@Mock([SecUser, Key])
class UserApiControllerSpec extends ControllerSpecification {

	SecUser me
	PasswordEncoder passwordEncoder = new UnitTestPasswordEncoder()

	def setup() {
		me = new SecUser(
			name: "me",
			username: "me@too.com",
			enabled: true,
			password: passwordEncoder.encodePassword("foobar123!"),
		)
		me.id = 1
		me.save(validate: false)
		controller.passwordEncoder = passwordEncoder
	}

	void "unauthenticated user gets back 401"() {
		when:
		unauthenticated {
			controller.getUserInfo()
		}
		then:
		response.status == 401
	}

	void "authenticated user gets back specified user info from /me"() {
		when:
		authenticatedAs(me) { controller.getUserInfo() }
		then:
		response.json.name == me.name
		response.json.username == me.username
		!response.json.hasProperty("password")
		!response.json.hasProperty("id")
	}

	void "authenticated anonymous key gets back the key info from /me"() {
		Key key = new Key(name: 'anonymous-key')
		when:
		authenticatedAs(key) { controller.getUserInfo() }
		then:
		response.json.name == key.name
		response.json.id == key.id
		!response.json.hasProperty("password")
	}

	void "delete user account"() {
		setup:
		controller.userService = Mock(UserService)

		when:
		request.apiUser = me
		request.method = "DELETE"
		request.requestURI = "/api/v1/users/me"
		params.id = me.id
		authenticatedAs(me) { controller.delete() }

		then:
		1 * controller.userService.delete(me)
		response.status == 204
	}

	void "changing user settings must change them"() {
		controller.userService = new UserService()
		when: "new settings are submitted"
		request.method = "PUT"
		request.requestURI = "/api/v1/users/me"
		request.json = [
			name: "Changed Name",
		]
		request.apiUser = me
		authenticatedAs(me) {
			controller.update(new UpdateProfileCommand(name: "Changed Name"))
		}

		then: "values must be updated and show update message"
		SecUser.get(1).name == "Changed Name"
		response.json.name == "Changed Name"
	}

	void "sensitive fields cannot be changed"() {
		controller.userService = new UserService()

		when:
		request.method = "PUT"
		request.requestURI = "/api/v1/users/me"
		request.json = [
			username: "attacker@email.com",
			enabled: false,
		]
		request.apiUser = me
		authenticatedAs(me) {
			controller.update(new UpdateProfileCommand())
		}

		then:
		SecUser.get(1).username == "me@too.com"
		response.json.username == "me@too.com"
		SecUser.get(1).enabled
	}

	void "submitting valid content in user password change form must change user password"() {
		when: "password change form is submitted"
		def cmd = new ChangePasswordCommand(username: me.username, currentpassword: "foobar123!", password: "barbar123!", password2: "barbar123!")
		cmd.passwordEncoder = passwordEncoder
		cmd.userService = new UserService() {
			SecUser getUserFromUsernameAndPassword(String username, String password) throws InvalidUsernameAndPasswordException {
				return me
			}
		}
		request.method = "POST"
		authenticatedAs(me) {
			controller.changePassword(cmd)
		}
		then: "password must be changed"
		cmd.passwordEncoder.isPasswordValid(SecUser.get(1).password, "barbar123!")
		then:
		response.status == 204
	}

	void "uploadAvatarImage() responds with 400 and PARAMETER_MISSING if file not given"() {
		setup:
		controller.userAvatarImageService = Mock(UserAvatarImageService)
		request.apiUser = new SecUser()
		request.method = "POST"
		request.requestURI = "/api/v1/users/me/image"

		when:
		withFilters(action: "uploadAvatarImage") {
			controller.uploadAvatarImage()
		}

		then:
		0 * controller.userAvatarImageService._
		def e = thrown(ApiException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "uploadAvatarImage() invokes replaceImage()"() {
		setup:
		controller.userAvatarImageService = Mock(UserAvatarImageService)
		request.apiUser = new SecUser()
		request.method = "POST"
		request.requestURI = "/api/v1/users/me/image"
		def bytes = new byte[16]
		request.addFile(new MockMultipartFile("file", "my-user-avatar-image.jpg", "image/jpeg", bytes))

		when:
		withFilters(action: "uploadAvatarImage") {
			controller.uploadAvatarImage()
		}

		then:
		1 * controller.userAvatarImageService.replaceImage((SecUser) request.apiUser, bytes, "my-user-avatar-image.jpg")
	}

	void "uploadAvatarImage() returns 200 and renders user"() {
		controller.userAvatarImageService = Mock(UserAvatarImageService)
		request.apiUser = new SecUser(username: "foo@ƒoo.bar")
		request.method = "POST"
		request.requestURI = "/api/v1/users/me/image"
		def bytes = new byte[16]
		request.addFile(new MockMultipartFile("file", "my-user-avatar-image.jpg", "image/jpeg", bytes))

		when:
		withFilters(action: "uploadAvatarImage") {
			controller.uploadAvatarImage()
		}

		then:
		response.status == 200
		response.json.username == request.apiUser.username
	}

	void "submitting an invalid current password won't let the password be changed"() {
		when: "password change form is submitted with invalid password"
		def cmd = new ChangePasswordCommand(username: me.username, currentpassword: "invalid", password: "barbar123!", password2: "barbar123!")
		cmd.passwordEncoder = passwordEncoder
		cmd.userService = new UserService() {
			SecUser getUserFromUsernameAndPassword(String username, String password) throws InvalidUsernameAndPasswordException {
				throw new InvalidUsernameAndPasswordException("mocked: invalid current password!")
			}
		}
		request.method = "POST"
		authenticatedAs(me) {
			controller.changePassword(cmd)
		}
		then: "the old password must remain valid"
		cmd.passwordEncoder.isPasswordValid(SecUser.get(1).password, "foobar123!")
		then:
		def e = thrown(ApiException)
		e.message == "Password not changed!"
		e.code == "PASSWORD_CHANGE_FAILED"
		e.statusCode == 400
	}

	void "submitting a too short new password won't let the password be changed"() {
		when: "password change form is submitted with invalid new password"
		def cmd = new ChangePasswordCommand(username: me.username, currentpassword: "foobar", password: "asd", password2: "asd")
		cmd.passwordEncoder = passwordEncoder
		cmd.userService = new UserService() {
			SecUser getUserFromUsernameAndPassword(String username, String password) throws InvalidUsernameAndPasswordException {
				return me
			}
		}
		request.method = "POST"
		authenticatedAs(me) {
			controller.changePassword(cmd)
		}
		then: "the old password must remain valid"
		cmd.passwordEncoder.isPasswordValid(SecUser.get(1).password, "foobar123!")
		then:
		def e = thrown(ApiException)
		e.message == "Password not changed!"
		e.code == "PASSWORD_CHANGE_FAILED"
		e.statusCode == 400
	}

	void "submitting a too weak new password won't let the password be changed"() {
		when: "password change form is submitted with invalid new password"
		def cmd = new ChangePasswordCommand(currentpassword: "foobar123", password: "asd", password2: "asd")
		cmd.passwordEncoder = passwordEncoder
		cmd.userService = new UserService() {
			SecUser getUserFromUsernameAndPassword(String username, String password) throws InvalidUsernameAndPasswordException {
				return me
			}
		}
		request.method = "POST"
		authenticatedAs(me) {
			controller.changePassword(cmd)
		}
		then: "the old password must remain valid"
		cmd.passwordEncoder.isPasswordValid(SecUser.get(1).password, "foobar123!")
		then:
		def e = thrown(ApiException)
		e.message == "Password not changed!"
		e.code == "PASSWORD_CHANGE_FAILED"
		e.statusCode == 400
	}
}
