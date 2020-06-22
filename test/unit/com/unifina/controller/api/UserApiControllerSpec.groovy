package com.unifina.controller.api

import com.unifina.ControllerSpecification
import com.unifina.api.ApiException
import com.unifina.api.InvalidUsernameAndPasswordException
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.security.PasswordEncoder
import com.unifina.service.SessionService
import com.unifina.service.UserAvatarImageService
import com.unifina.service.UserService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.FiltersUnitTestMixin
import org.springframework.mock.web.MockMultipartFile

@TestFor(UserApiController)
@Mock([SecUser, Key, UnifinaCoreAPIFilters])
@TestMixin(FiltersUnitTestMixin)
class UserApiControllerSpec extends ControllerSpecification {

	SecUser me
	PasswordEncoder passwordEncoder = new UnitTestPasswordEncoder()
	SessionService sessionService

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
		sessionService = mockBean(SessionService, Mock(SessionService))
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
		request.addHeader("Authorization", "Bearer token")
		request.apiUser = me
		request.method = "DELETE"
		request.requestURI = "/api/v1/users/me"
		params.id = me.id

		when:
		withFilters(action: "delete") {
			controller.delete()
		}

		then:
		1 * sessionService.getUserishFromToken("token") >> request.apiUser
		1 * controller.userService.delete(me)
		response.status == 204
	}

	void "changing user settings must change them"() {
		setup:
		controller.userService = new UserService()
		request.addHeader("Authorization", "Bearer token")
		request.method = "PUT"
		request.requestURI = "/api/v1/users/me"
		request.json = [
			name: "Changed Name",
		]
		request.apiUser = me

		when: "new settings are submitted"
		withFilters(action: "update") {
			controller.update()
		}

		then: "values must be updated and show update message"
		1 * sessionService.getUserishFromToken("token") >> request.apiUser
		SecUser.get(1).name == "Changed Name"
		response.json.name == "Changed Name"
	}

	void "sensitive fields cannot be changed"() {
		setup:
		controller.userService = new UserService()
		request.addHeader("Authorization", "Bearer token")
		request.method = "PUT"
		request.requestURI = "/api/v1/users/me"
		request.json = [
			username: "attacker@email.com",
			enabled: false,
		]
		request.apiUser = me

		when:
		withFilters(action: "update") {
			controller.update()
		}

		then:
		1 * sessionService.getUserishFromToken("token") >> request.apiUser
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
		request.apiUser = new SecUser(username: "foo@ƒoo.bar")
		request.method = "POST"
		request.requestURI = "/api/v1/users/me/image"
		request.addHeader("Authorization", "Bearer token")
		request.addHeader("Content-Length", 200)
		request.setContentType("multipart/form-data")

		when:
		withFilters(action: "uploadAvatarImage") {
			controller.uploadAvatarImage()
		}

		then:
		1 * sessionService.getUserishFromToken("token") >> request.apiUser
		0 * controller.userAvatarImageService._
		def e = thrown(ApiException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "uploadAvatarImage() invokes replaceImage()"() {
		setup:
		controller.userAvatarImageService = Mock(UserAvatarImageService)
		request.apiUser = new SecUser(username: "foo@ƒoo.bar")
		request.addHeader("Authorization", "Bearer token")
		request.method = "POST"
		request.requestURI = "/api/v1/users/me/image"
		def bytes = new byte[16]
		request.addFile(new MockMultipartFile("file", "my-user-avatar-image.jpg", "image/jpeg", bytes))
		request.addHeader("Content-Length", bytes.length)

		when:
		withFilters(action: "uploadAvatarImage") {
			controller.uploadAvatarImage()
		}

		then:
		1 * sessionService.getUserishFromToken("token") >> request.apiUser
		1 * controller.userAvatarImageService.replaceImage(request.apiUser as SecUser, bytes, "my-user-avatar-image.jpg")
	}

	void "uploadAvatarImage() returns 200 and renders user"() {
		setup:
		controller.userAvatarImageService = Mock(UserAvatarImageService)
		request.method = "POST"
		request.requestURI = "/api/v1/users/me/image"
		def bytes = new byte[16]
		request.addFile(new MockMultipartFile("file", "my-user-avatar-image.jpg", "image/jpeg", bytes))
		request.addHeader("Content-Length", bytes.length)
		request.addHeader("Authorization", "Bearer token")

		when:
		withFilters(action: "uploadAvatarImage") {
			controller.uploadAvatarImage()
		}

		then:
		1 * sessionService.getUserishFromToken("token") >> new SecUser(username: "foo@ƒoo.bar")
		response.status == 200
		response.json.username == request.apiUser.username
	}

	void "uploadAvatarImage accepts image uploads with correct request content type"() {
		setup:
		controller.userAvatarImageService = Mock(UserAvatarImageService)
		request.addHeader("Authorization", "Bearer token")
		request.method = "POST"
		request.requestURI = "/api/v1/users/me/image"
		def bytes = new byte[16]
		request.addFile(new MockMultipartFile("file", "my-user-avatar-image.jpg", "image/jpeg", bytes))
		request.addHeader("Content-Length", bytes.length)
		request.setContentType("multipart/form-data")
		when:
		withFilters(action: "uploadAvatarImage") {
			controller.uploadAvatarImage()
		}
		then:
		1 * sessionService.getUserishFromToken("token") >> new SecUser(username: "foo@ƒoo.bar")
		response.status == 200
	}

	void "uploadAvatarImage doesnt accept unlisted request content type"() {
		setup:
		controller.userAvatarImageService = Mock(UserAvatarImageService)
		request.addHeader("Authorization", "Bearer token")
		request.method = "POST"
		request.requestURI = "/api/v1/users/me/image"
		def bytes = new byte[16]
		request.addFile(new MockMultipartFile("file", "my-user-avatar-image.jpg", "image/jpeg", bytes))
		request.addHeader("Content-Length", bytes.length)
		request.setContentType("foobar/no-such-content-type")
		when:
		withFilters(action: "uploadAvatarImage") {
			controller.uploadAvatarImage()
		}
		then:
		1 * sessionService.getUserishFromToken("token") >> new SecUser(username: "foo@ƒoo.bar")
		response.status == 415
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
