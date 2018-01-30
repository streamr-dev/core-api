package com.unifina.controller.tour

import com.unifina.domain.security.SecUser
import com.unifina.domain.tour.TourUser

import grails.test.mixin.*
import grails.test.mixin.support.*
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

import org.junit.*

@TestFor(TourUserController)
@Mock([TourUser, SecUser])
class TourUserControllerSpec extends Specification {

	SecUser u1 = new SecUser(id: 1, username: "test1", password: "test")
	SecUser u2 = new SecUser(id: 2, username: "test2", password: "test")

	def springSecurityService = [
		currentUser: u1,
		encodePassword: { password ->
			return password
		}
	]

	void setup() {
		springSecurityService.currentUser.save(validate: false, failOnError: true)
	}

	void "should return user's and only his completed tours"() {
		when:
		params.tourNumber = 3
		request.method = "POST"
		controller.springSecurityService = springSecurityService
		controller.completed()
		then:
		response.status == 200

		when:
		params.tourNumber = 4
		springSecurityService.currentUser = u2
		request.method = "POST"
		controller.springSecurityService = springSecurityService
		controller.completed()
		then:
		response.status == 200

		when:
		request.method = "GET"
		controller.list()
		then:
		response.status == 200
		response.text == "[4]"
	}
}
