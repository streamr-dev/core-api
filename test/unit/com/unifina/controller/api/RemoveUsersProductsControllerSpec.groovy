package com.unifina.controller.api

import com.unifina.FilterMockingSpecification
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecRole
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SecUserSecRole
import com.unifina.service.ProductService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(RemoveUsersProductsController)
@Mock([SecUser, Key, SecRole, SecUserSecRole])
class RemoveUsersProductsControllerSpec extends FilterMockingSpecification {
	ProductService productService
	SecUser me

	def setup() {
		productService = controller.productService = Mock(ProductService)
		me = new SecUser(id: 1, username: "arnold").save(validate: false)
		Key key = new Key(name: "key", user: me)
		key.id = "myApiKey"
		key.save(failOnError: true, validate: true)
	}

	def "should remove users troll products if user is admin"() {
		def role = new SecRole(authority: "ROLE_ADMIN").save(failOnError: true)
		new SecUserSecRole(secUser: me, secRole: role).save(failOnError: true)
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
		def role = new SecRole(authority: "ROLE_USER").save(failOnError: true)
		new SecUserSecRole(secUser: me, secRole: role).save(failOnError: true)
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
