package com.unifina.controller.api

import com.unifina.api.ProductListParams
import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.Product
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.ApiService
import com.unifina.service.ProductService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse

@TestFor(ProductApiController)
@Mock([UnifinaCoreAPIFilters])
class ProductApiControllerSpec extends Specification {

	Product product

	void setup() {
		def category = new Category(name: "category")
		category.id = "category-id"

		product = new Product(
			name: "product",
			description: "description",
			category: category,
			ownerAddress: "0x0",
			beneficiaryAddress: "0x1",
			pricePerSecond: 5,
		)
		product.id = "product-id"
	}

	void "index() invokes productService#list"() {
		controller.apiService = new ApiService()
		def productService = controller.productService = Mock(ProductService)

		request.addHeader("Authorization", "token myApiKey")
		when:
		withFilters(action: "index") {
			controller.index()
		}
		then:
		1 * productService.list(_ as ProductListParams, null) >> []
	}

	void "index() invokes apiService#addLinkHintToHeader"() {
		def apiService = controller.apiService = Mock(ApiService)
		controller.productService = Stub(ProductService) {
			list(_, _) >> [product]
		}

		when:
		withFilters(action: "index") {
			controller.index()
		}
		then:
		1 * apiService.addLinkHintToHeader(_ as ProductListParams, 1, _ as Map, _ as HttpServletResponse)
	}

	void "index() returns 200 and renders products"() {
		controller.productService = Stub(ProductService) {
			list(_, _) >> [product]
		}
		controller.apiService = new ApiService()

		when:
		withFilters(action: "index") {
			controller.index()
		}
		then:
		response.status == 200
		response.json == [
			product.toMap()
		]
	}
}
