package com.unifina.controller.api

import com.unifina.ControllerSpecification
import com.unifina.api.ApiException
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.service.ProductImageService
import com.unifina.service.ProductService
import com.unifina.service.UserAvatarImageService
import com.unifina.service.UserService
import grails.test.mixin.TestFor
import org.springframework.mock.web.MockMultipartFile

@TestFor(UserApiController)
class UserApiControllerSpec extends ControllerSpecification {

	SecUser me

	def setup() {
		me = new SecUser(
			name: "me",
			username: "me@too.com",
			enabled: true,
		)
		me.id = 1
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

	void "delete user account"() {
		setup:
		controller.userService = Mock(UserService)

		when:
		request.apiUser = me
		request.method = "DELETE"
		request.requestURI = "/api/v1/users/me/1"
		params.id = me.id
		authenticatedAs(me) { controller.delete(me.id) }

		then:
		1 * controller.userService.delete(me, me.id)
		response.status == 204
	}

	void "uploadAvatarImage() responds with 400 and PARAMETER_MISSING if file not given"() {
		setup:
		controller.userAvatarImageService = Mock(UserAvatarImageService)
		request.apiUser = new SecUser()
		request.method = "POST"
		request.requestURI = "/api/v1/users/me/images"

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
		request.requestURI = "/api/v1/users/me/images"
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
		request.apiUser = new SecUser()
		request.method = "POST"
		request.requestURI = "/api/v1/users/me/images"
		def bytes = new byte[16]
		request.addFile(new MockMultipartFile("file", "my-user-avatar-image.jpg", "image/jpeg", bytes))

		when:
		withFilters(action: "uploadAvatarImage") {
			controller.uploadAvatarImage()
		}

		then:
		response.status == 200
		response.json == ((SecUser) request.apiUser).toMap()
	}

}
