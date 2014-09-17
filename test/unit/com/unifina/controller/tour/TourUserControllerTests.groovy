package com.unifina.controller.tour

import com.unifina.domain.security.SecUser
import com.unifina.domain.tour.TourUser

import grails.test.mixin.*
import grails.test.mixin.support.*

import java.nio.file.Files
import java.nio.file.Path

import org.junit.*

@TestFor(TourUserController)
@Mock([TourUser, SecUser])
class TourUserControllerTests {

	def u1 = new SecUser(id: 1, username: "test1", password: "test")
	def u2 = new SecUser(id: 2, username: "test2", password: "test")

	def springSecurityService = [
		currentUser: u1,
		encodePassword: { password ->
			return password
		}
	]

	void setUp() {
		springSecurityService.currentUser.springSecurityService = springSecurityService
		springSecurityService.currentUser.save(validate: false, failOnError: true)
	}

	void "test: it should return user's and only his completed tours"() {
		params.tourNumber = 3
		request.method = "POST"
		controller.springSecurityService = springSecurityService
		controller.completed()
		assert response.status == 200

		params.tourNumber = 4
		springSecurityService.currentUser = u2
		request.method = "POST"
		controller.springSecurityService = springSecurityService
		controller.completed()
		assert response.status == 200

		request.method = "GET"
		controller.list()

		assert response.status == 200
		assert response.text == "[4]"
	}
}
