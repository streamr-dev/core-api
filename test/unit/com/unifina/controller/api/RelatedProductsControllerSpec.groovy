package com.unifina.controller.api

import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.ProductService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(ProductApiController)
@Mock([UnifinaCoreAPIFilters, Product, SecUser, Category])
class RelatedProductsControllerSpec extends Specification {
	Product newProduct(String id, String name, String description, Category c, SecUser user) {
		Product p = new Product(
			name: name,
			description: description,
			imageUrl: "http://www.img.com/foo.jpg",
			thumbnailUrl: "http://www.img.com/foo.jpg",
			category: c,
			dateCreated: new Date(),
			lastUpdated: new Date(),
			score: 0,
			owner: user,
			ownerAddress: null,
			beneficiaryAddress: null,
			pricePerSecond: 0,
			minimumSubscriptionInSeconds: 0,
			blockNumber: 0,
			blockIndex: 0
		)
		p.id = id
		p.save(validate: true, failOnError: true)
		return p
	}

	Product p1, p2, p3, p4
	SecUser u1, u2, apiUser
	Category cat1, cat2

	void setup() {
		controller.productService = Mock(ProductService)
		apiUser = new SecUser(
			username: "username: api@user.com",
			name: "Regular API user",
			password: "xxx"
		)
		def key = new Key(name: "meKey", user: apiUser)
		key.id = "myApiKey"
		key.save(failOnError: true, validate: true)

		// u1 is the user who owns the product p1 used to search for related products
		u1 = new SecUser(
			username: "username: masa@hypätääneka.com",
			name: "Matti Nykänen",
			password: "xxx"
		)
		u1.id = 1
		u1.save(validate: false, failOnError: true)

		u2 = new SecUser(
			username: "username: marilyn@monroe.com",
			name: "Marilyn Monroe",
			password: "xxx"
		)
		u2.id = 2
		u2.save(validate: false, failOnError: true)

		cat1 = new Category(name: "Car Category")
		cat1.id = "car-category"
		cat1.save(validate: true, failOnError: true)

		cat2 = new Category(name: "Teapot Category")
		cat2.id = "teapot-category"
		cat2.save(validate: true, failOnError: true)

		// p1 is the product used to search for related products
		p1 = newProduct("p1-id", "car data", "data of cars", cat1, u1)
		p2 = newProduct("p2-id", "F1 car data", "data of F1 cars", cat1, u2)
		p3 = newProduct("p3-id", "Teapot data", "data of teapots", cat2, u1)
		p4 = newProduct("p4-id", "Rally car data", "data of rally cars", cat1, u2)
	}

	def "max param has default value of three"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.method = "GET"
		params.id = p1.id
		request.requestURI = "/api/v1/products/${p1.id}/related"

		withFilters(action: "index") {
			controller.related()
		}
		then:
		1 * controller.productService.findById(p1.id, apiUser, Permission.Operation.READ) >> p1
		1 * controller.productService.relatedProducts(p1, 3, apiUser) >> [p2]
		response.status == 200
		response.json[0].id == p2.id
		response.json[0].name == p2.name
	}

	def "max param has maximum value of ten"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.method = "GET"
		params.id = p1.id
		params.max = "11"
		request.requestURI = "/api/v1/products/${p1.id}/related?max=20"

		withFilters(action: "index") {
			controller.related()
		}
		then:
		1 * controller.productService.findById(p1.id, apiUser, Permission.Operation.READ) >> p1
		1 * controller.productService.relatedProducts(p1, 10, apiUser) >> [p2]
		response.status == 200
		response.json[0].id == p2.id
		response.json[0].name == p2.name
	}

	def "show related products for a product"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.method = "GET"
		params.id = p1.id
		request.requestURI = "/api/v1/products/${p1.id}/related"

		withFilters(action: "index") {
			controller.related()
		}
		then:
		1 * controller.productService.findById(p1.id, apiUser, Permission.Operation.READ) >> p1
		1 * controller.productService.relatedProducts(p1, 3, apiUser) >> [p2, p3, p4]
		response.status == 200
		response.json[0].id == p2.id
		response.json[0].name == p2.name
		response.json[1].id == p3.id
		response.json[1].name == p3.name
		response.json[2].id == p4.id
		response.json[2].name == p4.name
	}

	def "unknown id parameter returns en empty list"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.method = "GET"
		params.id = "xxx-id-xxx"
		request.requestURI = "/api/v1/products/xxx-id-xxx/related"

		withFilters(action: "index") {
			controller.related()
		}
		then:
		1 * controller.productService.relatedProducts(null, 3, apiUser) >> new ArrayList<Product>()
		response.status == 200
		response.json == []
	}
}
