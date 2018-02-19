package com.unifina.controller.api

import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.ApiService
import com.unifina.service.ProductService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(ProductStreamsApiController)
@Mock([UnifinaCoreAPIFilters])
class ProductStreamsApiControllerSpec extends Specification {
	void "index() invokes productService#findById (READ)"() {
		def productService = controller.productService = Mock(ProductService)

		params.productId = "product-id"
		def user = request.apiUser = new SecUser(username: "me@streamr.com")
		when:
		withFilters(action: "index") {
			controller.index()
		}
		then:
		1 * productService.findById("product-id", user, Permission.Operation.READ) >> new Product()
	}

	void "index() returns 200 and list of streams"() {
		def feed = new Feed(name: "Feed")
		def s1 = new Stream(name: "Stream #1", partitions: 5, feed: feed)
		def s2 = new Stream(name: "Stream #2", partitions: 1, feed: feed)
		def s3 = new Stream(name: "Stream #3", partitions: 3, feed: feed)

		s1.id = "stream-1"
		s2.id = "stream-2"
		s3.id = "stream-3"

		controller.productService = Stub(ProductService) {
			findById(_, _, _) >> new Product(streams: [s1, s2, s3])
		}

		params.productId = "product-id"
		def user = request.apiUser = new SecUser(username: "me@streamr.com")

		when:
		withFilters(action: "index") {
			controller.index()
		}
		then:
		response.status == 200
		response.json as Set == [s1, s2, s3]*.toMap() as Set
	}

	void "update() invokes productService#findById (WRITE) and productService#addStreamToProduc"() {
		def productService = controller.productService = Mock(ProductService)
		controller.apiService = Stub(ApiService) {
			getByIdAndThrowIfNotFound(_, _) >> {
				def s = new Stream()
				s.id = "id"
				s
			}
		}
		def product = new Product()

		params.productId = "product-id"
		params.id = "stream-id"
		def user = request.apiUser = new SecUser(username: "me@streamr.com")

		when:
		withFilters(action: "update") {
			controller.update()
		}
		then:
		1 * productService.findById("product-id", user, Permission.Operation.WRITE) >> product
		1 * productService.addStreamToProduct(product, _ as Stream, user)
	}

	void "update() invokes apiService#getByIdAndThrowIfNotFound"() {
		controller.productService = Stub(ProductService)
		def apiService = controller.apiService = Mock(ApiService)

		params.productId = "product-id"
		params.id = "stream-id"
		request.apiUser = new SecUser(username: "me@streamr.com")

		when:
		withFilters(action: "update") {
			controller.update()
		}
		then:
		1 * apiService.getByIdAndThrowIfNotFound(Stream, "stream-id")
	}

	void "update() returns 204"() {
		controller.productService = Stub(ProductService)
		controller.apiService = Stub(ApiService) {
			getByIdAndThrowIfNotFound(_, _) >> null
		}

		when:
		withFilters(action: "update") {
			controller.update()
		}
		then:
		response.status == 204
	}

	void "delete() invokes productService#findById (WRITE) and productService#addStreamToProduc"() {
		def productService = controller.productService = Mock(ProductService)
		controller.apiService = Stub(ApiService) {
			getByIdAndThrowIfNotFound(_, _) >> {
				def s = new Stream()
				s.id = "id"
				s
			}
		}
		def product = new Product()

		params.productId = "product-id"
		params.id = "stream-id"
		def user = request.apiUser = new SecUser(username: "me@streamr.com")

		when:
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		1 * productService.findById("product-id", user, Permission.Operation.WRITE) >> product
		1 * productService.removeStreamFromProduct(product, _ as Stream)
	}

	void "delete() invokes apiService#getByIdAndThrowIfNotFound"() {
		controller.productService = Stub(ProductService)
		def apiService = controller.apiService = Mock(ApiService)

		params.productId = "product-id"
		params.id = "stream-id"
		request.apiUser = new SecUser(username: "me@streamr.com")

		when:
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		1 * apiService.getByIdAndThrowIfNotFound(Stream, "stream-id")
	}

	void "delete() returns 204"() {
		controller.productService = Stub(ProductService)
		controller.apiService = Stub(ApiService) {
			getByIdAndThrowIfNotFound(_, _) >> null
		}

		when:
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		response.status == 204
	}
}
