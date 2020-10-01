package com.unifina.controller

import com.unifina.domain.Category
import com.unifina.domain.User
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(CategoryApiController)
@Mock([Category])
class CategoryApiControllerSpec extends ControllerSpecification {

	User me

	def setup() {
		me = new User(id: 1).save(validate: false)
	}

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
		authenticatedAs(me) { controller.index() }

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
