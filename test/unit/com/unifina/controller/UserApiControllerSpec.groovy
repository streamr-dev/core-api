package com.unifina.controller

import com.unifina.domain.User
import com.unifina.service.ApiException
import com.unifina.service.EthereumUserService
import com.unifina.service.SessionService
import com.unifina.service.UserAvatarImageService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.FiltersUnitTestMixin
import org.springframework.mock.web.MockMultipartFile

@TestFor(UserApiController)
@Mock([User, RESTAPIFilters])
@TestMixin(FiltersUnitTestMixin)
class UserApiControllerSpec extends ControllerSpecification {
	User me
	User ethUser
	SessionService sessionService
	EthereumUserService ethereumUserService

	def setup() {
		me = new User(
			name: "me",
			username: "0x0000000000000000000000000000000000000001",
			email: "me@too.com",
			enabled: true,
		)
		me.id = 1
		me.save(validate: false)
		ethUser = new User(
			name: "eth",
			username: "0x0000000000000000000000000000000000000000",
			email: "eth@eth.com",
			enabled: true,
		)
		ethUser.id = 2
		ethUser.save(validate: false)
		sessionService = mockBean(SessionService, Mock(SessionService))
		ethereumUserService = mockBean(EthereumUserService, Mock(EthereumUserService))
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
		!response.json.hasProperty("id")
	}

	void "delete user account"() {
		setup:
		controller.userService = Mock(EthereumUserService)
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
		1 * sessionService.getUserFromToken("token") >> request.apiUser
		1 * controller.userService.delete(me)
		response.status == 204
	}

	void "update ethereum users profile"() {
		setup:
		controller.userService = new EthereumUserService()
		request.addHeader("Authorization", "Bearer token")
		request.method = "PUT"
		request.requestURI = "/api/v1/users/me"
		request.json = [
			name: "New Name",
			email: "changed@emailaddress.com",
		]
		request.apiUser = ethUser

		when: "updated profile is submitted"
		withFilters(action: "update") {
			controller.update()
		}

		then: "values must be updated"
		1 * sessionService.getUserFromToken("token") >> request.apiUser
		response.json.name == "New Name"
		response.json.email == "changed@emailaddress.com"
		response.json.username == "0x0000000000000000000000000000000000000000"
	}

	void "private user fields cannot be changed via update profile"() {
		setup:
		controller.userService = new EthereumUserService()
		request.addHeader("Authorization", "Bearer token")
		request.method = "PUT"
		request.requestURI = "/api/v1/users/me"
		request.json = [
			username: "0x0000000000000000000000000001110987654321",
			enabled: false,
		]
		request.apiUser = me

		when:
		withFilters(action: "update") {
			controller.update()
		}

		then:
		1 * sessionService.getUserFromToken("token") >> request.apiUser
		User.get(1).username == "0x0000000000000000000000000000000000000001"
		response.json.username == "0x0000000000000000000000000000000000000001"
		User.get(1).enabled
	}

	void "uploadAvatarImage() responds with 400 and PARAMETER_MISSING if file not given"() {
		setup:
		controller.userAvatarImageService = Mock(UserAvatarImageService)
		request.apiUser = new User(username: "foo@ƒoo.bar")
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
		1 * sessionService.getUserFromToken("token") >> request.apiUser
		0 * controller.userAvatarImageService._
		def e = thrown(ApiException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "uploadAvatarImage() invokes replaceImage()"() {
		setup:
		controller.userAvatarImageService = Mock(UserAvatarImageService)
		request.apiUser = new User(username: "foo@ƒoo.bar")
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
		1 * sessionService.getUserFromToken("token") >> request.apiUser
		1 * controller.userAvatarImageService.replaceImage(request.apiUser as User, bytes, "my-user-avatar-image.jpg")
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
		1 * sessionService.getUserFromToken("token") >> new User(username: "foo@ƒoo.bar")
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
		1 * sessionService.getUserFromToken("token") >> new User(username: "foo@ƒoo.bar")
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
		1 * sessionService.getUserFromToken("token") >> new User(username: "foo@ƒoo.bar")
		response.status == 415
	}
}
