package com.unifina.controller.api

import com.unifina.domain.security.Key
import com.unifina.domain.security.SecRole
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SecUserSecRole
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.ProductService
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(RemoveUsersProductsController)
@Mock([UnifinaCoreAPIFilters, SpringSecurityService, SecUser, Key, SecRole, SecUserSecRole])
class RemoveUsersProductsControllerSpec extends Specification {
	ProductService productService
	SecUser me

	void setup() {
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
		request.requestURI = "/api/v1/products/remove/sylvester"
		request.addHeader("Authorization", "Token myApiKey")
		params.username = "sylvester"
		withFilters(action: "index") {
			controller.index()
		}
		then:
		response.status == 204
		1 * productService.removeUsersProducts("sylvester")
	}

	def "should handle remove users products with invalid argument"() {
		when:
		request.method = "DELETE"
		request.requestURI = "/api/v1/products/remove"
		request.addHeader("Authorization", "Token myApiKey")
		params.username = null
		withFilters(action: "index") {
			controller.index()
		}
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
		request.requestURI = "/api/v1/products/remove/sylvester"
		request.addHeader("Authorization", "Token myApiKey")
		params.username = "sylvester"
		withFilters(action: "index") {
			controller.index()
		}
		then:
		response.status == 401
		0 * productService._
	}
}
