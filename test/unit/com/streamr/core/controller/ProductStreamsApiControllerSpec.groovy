package com.streamr.core.controller

import com.streamr.core.domain.Permission
import com.streamr.core.domain.Product
import com.streamr.core.domain.User
import com.streamr.core.service.ProductService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(ProductStreamsApiController)
@Mock([RESTAPIFilters, Product, Permission, User])
class ProductStreamsApiControllerSpec extends Specification {
	void "index() invokes productService#findById (product_get)"() {
		controller.productService = Mock(ProductService)
		params.productId = "product-id"
		request.apiUser = new User(username: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF")
		when:
		withFilters(action: "index") {
			controller.index(params.productId)
		}
		then:
		1 * controller.productService.findById("product-id", request.apiUser, Permission.Operation.PRODUCT_GET) >> new Product()
	}

	void "index() returns 200 and list of streams"() {
		def s1 = "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF/s1"
		def s2 = "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF/s2"
		def s3 = "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF/s3"

		controller.productService = Stub(ProductService) {
			findById(_, _, _) >> new Product(streams: [s1, s2, s3])
		}

		params.productId = "product-id"
		request.apiUser = new User(username: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF")

		when:
		withFilters(action: "index") {
			controller.index(params.productId)
		}
		then:
		response.status == 200
		response.json as Set == [s1, s2, s3] as Set
	}

	void "update() invokes productService#findById (product_edit) and productService#addStreamToProduc"() {
		controller.productService = Mock(ProductService)
		def product = new Product()
		product.id = "product-id"
		product.streams = []

		params.productId = "product-id"
		params.id = "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF/s1"
		request.apiUser = new User(username: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF")

		when:
		withFilters(action: "update") {
			controller.update(params.productId, params.id)
		}
		then:
		1 * controller.productService.findById("product-id", request.apiUser, Permission.Operation.PRODUCT_EDIT) >> product
		1 * controller.productService.addStreamToProduct(product, _ as String, request.apiUser)
	}

	void "update() returns 204"() {
		controller.productService = Mock(ProductService)
		User me = new User(username: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", name: "abc")
		me.save(failOnError: true, validate: true)
		def product = new Product()
		product.id = "product-id"
		product.streams = []
		product.owner = me
		product.save(validate: true, failOnError: true)

		params.productId = "product-id"
		params.streamId = "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF/s1"
		request.apiUser = me


		when:
		withFilters(action: "update") {
			controller.update(params.productId, params.id)
		}
		then:
		1 * controller.productService.findById(product.id, request.apiUser, Permission.Operation.PRODUCT_EDIT) >> product
		response.status == 204
	}

	void "delete() invokes productService#findById (product_edit) and productService#addStreamToProduc"() {
		controller.productService = Mock(ProductService)
		User me = new User(username: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", name: "abc")
		me.save(failOnError: true, validate: true)
		Product product = new Product()
		product.id = "product-id"
		product.streams = ["0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF/s1"]
		product.owner = me
		product.save(validate: true, failOnError: true)

		params.productId = product.id
		params.id = "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF/s1"
		request.apiUser = me

		when:
		withFilters(action: "delete") {
			controller.delete(params.productId, params.id)
		}
		then:
		1 * controller.productService.findById(product.id, me, Permission.Operation.PRODUCT_DELETE) >> product
		1 * controller.productService.removeStreamFromProduct(product, "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF/s1")
		response.status == 204
	}
}
