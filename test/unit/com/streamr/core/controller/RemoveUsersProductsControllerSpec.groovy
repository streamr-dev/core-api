package com.streamr.core.controller

import com.streamr.core.domain.Role
import com.streamr.core.domain.User
import com.streamr.core.domain.UserRole
import com.streamr.core.service.ProductService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(RemoveUsersProductsController)
@Mock([User, Role, UserRole])
class RemoveUsersProductsControllerSpec extends ControllerSpecification {
	ProductService productService
	User me

	def setup() {
		productService = controller.productService = Mock(ProductService)
		me = new User(id: 1, username: "arnold").save(validate: false)
	}

	def "should remove users troll products if user is admin"() {
		def role = new Role(authority: "ROLE_ADMIN").save(failOnError: true)
		new UserRole(user: me, role: role).save(failOnError: true)
		when:
		request.method = "DELETE"
		params.username = "sylvester"
		authenticatedAs(me) { controller.index() }

		then:
		response.status == 204
		1 * productService.removeUsersProducts("sylvester")
	}

	def "should handle remove users products with invalid argument"() {
		when:
		request.method = "DELETE"
		params.username = null
		authenticatedAs(me) { controller.index() }

		then:
		response.status == 400
		0 * productService._
	}

	def "regular user cannot remove troll products"() {
		def role = new Role(authority: "ROLE_USER").save(failOnError: true)
		new UserRole(user: me, role: role).save(failOnError: true)
		when:
		request.apiUser = me
		request.method = "DELETE"
		params.username = "sylvester"
		authenticatedAs(me) { controller.index() }

		then:
		response.status == 401
		0 * productService._
	}
}
