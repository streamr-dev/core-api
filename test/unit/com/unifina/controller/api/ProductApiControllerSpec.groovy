package com.unifina.controller.api

import com.unifina.api.CreateProductCommand
import com.unifina.api.ProductDeployedCommand
import com.unifina.api.ProductListParams
import com.unifina.api.ProductUndeployedCommand
import com.unifina.api.SetPricingCommand
import com.unifina.api.UpdateProductCommand
import com.unifina.api.ValidationException
import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
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

	void "show() invokes productService#findById"() {
		def productService = controller.productService = Mock(ProductService)

		params.id = "product-id"
		when:
		withFilters(action: "index") {
			controller.show()
		}
		then:
		1 * productService.findById('product-id', _, Permission.Operation.READ) >> product
	}

	void "show() returns 200 and renders a product"() {
		controller.productService = Stub(ProductService) {
			findById("product-id", _, Permission.Operation.READ) >> product
		}

		params.id = "product-id"
		when:
		withFilters(action: "index") {
			controller.show()
		}
		then:
		response.status == 200
		response.json == product.toMap()
	}

	void "save() invokes productService#create"() {
		def productService = controller.productService = Mock(ProductService)

		def user = request.apiUser = new SecUser()

		request.JSON == [:]
		when:
		withFilters(action: "save") {
			controller.save()
		}
		then:
		1 * productService.create(_ as CreateProductCommand, user) >> product
	}

	void "save() returns 200 and renders a product"() {
		controller.productService = Stub(ProductService) {
			create(_, _) >> product
		}

		def user = request.apiUser = new SecUser()

		request.JSON == [:]
		when:
		withFilters(action: "save") {
			controller.save()
		}
		then:
		response.status == 200
		response.json == product.toMap()
	}

	void "update() invokes productService#update"() {
		def productService = controller.productService = Mock(ProductService)

		def user = request.apiUser = new SecUser()

		params.id = "product-id"
		request.JSON == [:]
		when:
		withFilters(action: "update") {
			controller.update()
		}
		then:
		1 * productService.update("product-id", _ as UpdateProductCommand, user) >> product
	}

	void "update() returns 200 and renders a product"() {
		controller.productService = Stub(ProductService) {
			update("product-id", _, _) >> product
		}

		def user = request.apiUser = new SecUser()

		params.id = "product-id"
		request.JSON == [:]
		when:
		withFilters(action: "update") {
			controller.update()
		}
		then:
		response.status == 200
		response.json == product.toMap()
	}

	void "setDeploying() invokes productService#findById() and productService#transitionToDeploying()"() {
		def productService = controller.productService = Mock(ProductService)

		def user = request.apiUser = new SecUser()

		params.id = "product-id"
		request.method = "POST"
		when:
		withFilters(action: "setDeploying") {
			controller.setDeploying()
		}
		then:
		1 * productService.findById("product-id", user, Permission.Operation.WRITE) >> product
		1 * productService.transitionToDeploying(product)
	}

	void "setDeploying() returns 200 and renders a product"() {
		controller.productService = Stub(ProductService) {
			findById("product-id", _ as SecUser, Permission.Operation.WRITE) >> product
		}

		def user = request.apiUser = new SecUser()

		params.id = "product-id"
		request.JSON = [
				tx: "0x0000000000FFFFFFFFFF0000000000FFFFFFFFFF0000000000FFFFFFFFFFAAAA"
		]
		request.method = "POST"
		when:
		withFilters(action: "setDeploying") {
			controller.setDeploying()
		}
		then:
		response.status == 200
		response.json == product.toMap()
	}

	void "setDeployed() invokes apiService#getByIdAndThrowIfNotFound"() {
		def apiService = controller.apiService = Mock(ApiService)
		controller.productService = Stub(ProductService)

		request.apiUser = new SecUser()

		params.id = "product-id"
		request.JSON = [:]
		request.method = "POST"
		when:
		withFilters(action: "setDeployed") {
			controller.setDeployed()
		}
		then:
		1 * apiService.getByIdAndThrowIfNotFound(Product, "product-id") >> product
	}

	void "setDeployed() invokes productService#markAsDeployed"() {
		controller.apiService = Stub(ApiService) {
			getByIdAndThrowIfNotFound(Product, "product-id") >> product
		}
		def productService = controller.productService = Mock(ProductService)

		def user = request.apiUser = new SecUser()

		params.id = "product-id"
		request.JSON = [:]
		request.method = "POST"
		when:
		withFilters(action: "setDeployed") {
			controller.setDeployed()
		}
		then:
		1 * productService.markAsDeployed(product, _ as ProductDeployedCommand, user) >> product
	}

	void "setPricing() invokes productService#updatePricing"() {
		controller.apiService = Stub(ApiService) {
			getByIdAndThrowIfNotFound(Product, "product-id") >> product
		}
		def productService = controller.productService = Mock(ProductService)

		def user = request.apiUser = new SecUser()

		params.id = "product-id"
		request.JSON = [:]
		request.method = "POST"
		when:
		withFilters(action: "setPricing") {
			controller.setPricing()
		}
		then:
		1 * productService.updatePricing(product, _ as SetPricingCommand, user) >> product
	}

	void "setDeployed() returns 200 and renders a product"() {
		controller.apiService = Stub(ApiService) {
			getByIdAndThrowIfNotFound(Product, "product-id") >> product
		}
		controller.productService = Stub(ProductService) {
			markAsDeployed(_, _, _) >> product
		}

		params.id = "product-id"
		request.JSON = [:]
		request.method = "POST"
		when:
		withFilters(action: "setDeployed") {
			controller.setDeployed()
		}
		then:
		response.status == 200
		response.json == product.toMap()
	}

	void "setUndeploying() invokes productService#findById() and productService#transitionToUndeploying()"() {
		def productService = controller.productService = Mock(ProductService)

		def user = request.apiUser = new SecUser()

		params.id = "product-id"
		request.method = "POST"
		when:
		withFilters(action: "setUndeploying") {
			controller.setUndeploying()
		}
		then:
		1 * productService.findById("product-id", user, Permission.Operation.WRITE) >> product
		1 * productService.transitionToUndeploying(product)
	}

	void "setUndeploying() returns 200 and renders a product"() {
		controller.productService = Stub(ProductService) {
			findById("product-id", _ as SecUser, Permission.Operation.WRITE) >> product
		}

		def user = request.apiUser = new SecUser()

		params.id = "product-id"
		request.method = "POST"
		when:
		withFilters(action: "setUndeploying") {
			controller.setUndeploying()
		}
		then:
		response.status == 200
		response.json == product.toMap()
	}

	void "setUndeployed() invokes apiService#getByIdAndThrowIfNotFound"() {
		def apiService = controller.apiService = Mock(ApiService)
		controller.productService = Stub(ProductService)

		def user = request.apiUser = new SecUser()

		params.id = "product-id"
		request.JSON = [:]
		request.method = "POST"
		when:
		withFilters(action: "setUndeployed") {
			controller.setUndeployed()
		}
		then:
		1 * apiService.getByIdAndThrowIfNotFound(Product, "product-id") >> product
	}

	void "setUndeployed() invokes productService#markAsUndeployed"() {
		controller.apiService = Stub(ApiService) {
			getByIdAndThrowIfNotFound(Product, "product-id") >> product
		}
		def productService = controller.productService = Mock(ProductService)

		def user = request.apiUser = new SecUser()

		params.id = "product-id"
		request.JSON = [:]
		request.method = "POST"
		when:
		withFilters(action: "setUndeployed") {
			controller.setUndeployed()
		}
		then:
		1 * productService.markAsUndeployed(product, _ as ProductUndeployedCommand, user)
	}

	void "setUndeployed() returns 200 and renders a product"() {
		controller.apiService = Stub(ApiService) {
			getByIdAndThrowIfNotFound(Product, "product-id") >> product
		}
		controller.productService = Stub(ProductService)

		def user = request.apiUser = new SecUser()

		params.id = "product-id"
		request.JSON = [:]
		request.method = "POST"
		when:
		withFilters(action: "setUndeployed") {
			controller.setUndeployed()
		}
		then:
		response.status == 200
		response.json == product.toMap()
	}
}
