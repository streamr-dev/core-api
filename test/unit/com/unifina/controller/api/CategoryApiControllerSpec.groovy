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

	void "lists categories"() {
		Category c1 = new Category(name: "Traffic", defaultImageUrl: "traffic.png")
		c1.id = "traffic"
		c1.save(failOnError: true, validate: true)

		Category c2 = new Category(name: "Cryptocurrency")
		c2.id = "crypto"
		c2.save(failOnError: true, validate: true)

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
				id: "traffic",
				name: "Traffic",
				defaultImageUrl: "traffic.png"
			],
			[
				id: "crypto",
				name: "Cryptocurrency",
				defaultImageUrl: null
			]
		]
	}

}
