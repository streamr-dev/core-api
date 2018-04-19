package com.unifina.controller.api

import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.ProductService
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(RemoveUsersProductsController)
@Mock([UnifinaCoreAPIFilters, SpringSecurityService, SecUser, Key])
class RemoveUsersProductsControllerSpec extends Specification {
	ProductService productService

	void setup() {
		productService = controller.productService = Mock(ProductService)
		SecUser me = new SecUser(id: 1, username: "arnold").save(validate: false)
		Key key = new Key(name: "key", user: me)
		key.id = "myApiKey"
		key.save(failOnError: true, validate: true)
	}

	def "should remove users products"() {
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
}
