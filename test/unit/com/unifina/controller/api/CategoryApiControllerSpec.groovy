package com.unifina.controller.api

import com.unifina.domain.marketplace.Category
import com.unifina.filters.UnifinaCoreAPIFilters
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(CategoryApiController)
@Mock([Category, UnifinaCoreAPIFilters, SpringSecurityService])
class CategoryApiControllerSpec extends Specification {

	void "lists categories in alphabetical order"() {
		Category c1 = new Category(name: "Traffic", imageUrl: "traffic.png")
		c1.id = "id-1"
		c1.save(failOnError: true, validate: true)

		Category c2 = new Category(name: "Cryptocurrency")
		c2.id = "id-2"
		c2.save(failOnError: true, validate: true)

		Category c3 = new Category(name: "Automobile")
		c3.id = "id-3"
		c3.save(failOnError: true, validate: true)

		when:
		request.requestURI = "/api/v1/categories"
		request.method = "GET"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		response.status == 200
		response.json == [
			[
				id: "id-3",
				name: "Automobile",
				imageUrl: null
			],
			[
				id: "id-2",
				name: "Cryptocurrency",
				imageUrl: null
			],
			[
				id: "id-1",
				name: "Traffic",
				imageUrl: "traffic.png"
			],
		]
	}

}
