package com.unifina.controller

import com.unifina.domain.Category
import com.unifina.domain.Permission
import com.unifina.domain.Product
import com.unifina.domain.User
import com.unifina.service.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.springframework.mock.web.MockMultipartFile
import spock.lang.Specification

@TestFor(ProductApiController)
@Mock([RESTAPIFilters, User])
class ProductApiControllerSpec extends Specification {

	Product product

	void setup() {
		def category = new Category(name: "category")
		category.id = "category-id"
		User user = new User(
			username: "user@domain.com",
			name: "Firstname Lastname",
		)
		user.id = 1
		user.save(failOnError: true, validate: false)
		product = new Product(
			name: "product",
			description: "description",
			category: category,
			ownerAddress: "0x0",
			beneficiaryAddress: "0x1",
			pricePerSecond: 5,
			owner: user,
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
			product.toSummaryMap()
		]
	}

	void "show() invokes productService#findById"() {
		controller.permissionService = Mock(PermissionService)
		def productService = controller.productService = Mock(ProductService)

		params.id = "product-id"
		when:
		withFilters(action: "show") {
			controller.show()
		}
		then:
		1 * productService.findById('product-id', _, Permission.Operation.PRODUCT_GET) >> product
		1 * controller.permissionService.check(_, _, Permission.Operation.PRODUCT_SHARE) >> false
	}

	void "show() returns 200 and renders a product"() {
		controller.permissionService = Mock(PermissionService)
		controller.productService = Stub(ProductService) {
			findById("product-id", _, Permission.Operation.PRODUCT_GET) >> product
		}

		params.id = "product-id"
		when:
		withFilters(action: "show") {
			controller.show()
		}
		then:
		response.status == 200
		response.json == product.toMap()
	}

	void "save() invokes productService#create"() {
		def productService = controller.productService = Mock(ProductService)

		def user = request.apiUser = new User()

		request.JSON == [:]
		when:
		withFilters(action: "save") {
			controller.save()
		}
		then:
		1 * productService.create(_ as ProductCreateCommand, user) >> product
	}

	void "save() returns 200 and renders a product"() {
		controller.productService = Stub(ProductService) {
			create(_, _) >> product
		}

		def user = request.apiUser = new User()

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
		controller.permissionService = Mock(PermissionService)
		def productService = controller.productService = Mock(ProductService)

		def user = request.apiUser = new User()

		params.id = "product-id"
		request.JSON == [:]
		when:
		withFilters(action: "update") {
			controller.update()
		}
		then:
		1 * productService.update("product-id", _ as ProductUpdateCommand, user) >> product
		1 * controller.permissionService.check(_, _, Permission.Operation.PRODUCT_SHARE) >> false
	}

	void "update() returns 200 and renders a product"() {
		controller.permissionService = Mock(PermissionService)
		controller.productService = Stub(ProductService) {
			update("product-id", _, _) >> product
		}

		def user = request.apiUser = new User()

		params.id = "product-id"
		request.JSON == [:]
		when:
		withFilters(action: "update") {
			controller.update()
		}
		then:
		response.status == 200
		response.json == product.toMap()
		1 * controller.permissionService.check(_, _, Permission.Operation.PRODUCT_SHARE) >> false
	}

	void "setDeploying() invokes productService#findById() and productService#transitionToDeploying()"() {
		def productService = controller.productService = Mock(ProductService)

		def user = request.apiUser = new User()

		params.id = "product-id"
		request.method = "POST"
		when:
		withFilters(action: "setDeploying") {
			controller.setDeploying()
		}
		then:
		1 * productService.findById("product-id", user, Permission.Operation.PRODUCT_EDIT) >> product
		1 * productService.transitionToDeploying(product)
	}

	void "setDeploying() returns 200 and renders a product"() {
		controller.productService = Stub(ProductService) {
			findById("product-id", _ as User, Permission.Operation.PRODUCT_EDIT) >> product
		}

		def user = request.apiUser = new User()

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

		request.apiUser = new User()

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

		def user = request.apiUser = new User()

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

		def user = request.apiUser = new User()

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

		def user = request.apiUser = new User()

		params.id = "product-id"
		request.method = "POST"
		when:
		withFilters(action: "setUndeploying") {
			controller.setUndeploying()
		}
		then:
		1 * productService.findById("product-id", user, Permission.Operation.PRODUCT_EDIT) >> product
		1 * productService.transitionToUndeploying(product)
	}

	void "setUndeploying() returns 200 and renders a product"() {
		controller.productService = Stub(ProductService) {
			findById("product-id", _ as User, Permission.Operation.PRODUCT_EDIT) >> product
		}

		def user = request.apiUser = new User()

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

		def user = request.apiUser = new User()

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

		def user = request.apiUser = new User()

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

		def user = request.apiUser = new User()

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

	void "uploadImage() responds with 400 and PARAMETER_MISSING if file not given"() {
		def productService = controller.productService = Mock(ProductService)
		controller.productImageService = Stub(ProductImageService)

		def user = request.apiUser = new User()

		params.id = "product-id"
		request.method = "POST"
		when:
		withFilters(action: "uploadImage") {
			controller.uploadImage()
		}
		then:
		def e = thrown(ApiException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "uploadImage() invokes productService#findById"() {
		def productService = controller.productService = Mock(ProductService)
		controller.productImageService = Stub(ProductImageService)

		def user = request.apiUser = new User()

		params.id = "product-id"
		request.method = "POST"
		request.addFile(new MockMultipartFile("file", "my-product-image.jpg", "image/jpeg", new byte[2048]))
		when:
		withFilters(action: "uploadImage") {
			controller.uploadImage()
		}
		then:
		1 * productService.findById("product-id", user, Permission.Operation.PRODUCT_EDIT) >> product
	}

	void "uploadImage() invokes productImageService#replaceImage"() {
		controller.productService = Stub(ProductService) {
			findById(_, _, _) >> product
		}
		def productImageService = controller.productImageService = Mock(ProductImageService)

		def bytes = new byte[16]

		params.id = "product-id"
		request.method = "POST"
		request.addFile(new MockMultipartFile("file", "my-product-image.jpg", "image/jpeg", bytes))
		when:
		withFilters(action: "uploadImage") {
			controller.uploadImage()
		}
		then:
		1 * productImageService.replaceImage(product, bytes, "my-product-image.jpg")
	}

	void "uploadImage() returns 200 and renders a product"() {
		controller.productService = Stub(ProductService) {
			findById(_, _, _) >> product
		}
		controller.productImageService = Stub(ProductImageService)

		def bytes = new byte[16]

		params.id = "product-id"
		request.method = "POST"
		request.addFile(new MockMultipartFile("file", "my-product-image.jpg", "image/jpeg", bytes))
		when:
		withFilters(action: "uploadImage") {
			controller.uploadImage()
		}

		then:
		response.status == 200
		response.json == product.toMap()
	}

	void "deployFree() invokes productService#findById (with SHARE permission requirement)"() {
		def productService = controller.productService = Mock(ProductService)
		controller.productFreeService = Stub(ProductFreeService)
		def user = new User()

		params.id = "product-id"
		request.method = "POST"
		request.apiUser = user
		when:
		withFilters(action: "deployFree") {
			controller.deployFree()
		}
		then:
		1 * productService.findById('product-id', user, Permission.Operation.PRODUCT_SHARE) >> product
	}

	void "deployFree() invokes freeProductService#deployFreeProduct"() {
		controller.productService = Stub(ProductService) {
			findById(_, _, _) >> product
		}
		def freeProductService = controller.productFreeService = Mock(ProductFreeService)

		params.id = "product-id"
		request.method = "POST"
		request.apiUser = new User()
		when:
		withFilters(action: "deployFree") {
			controller.deployFree()
		}
		then:
		1 * freeProductService.deployFreeProduct(product)
	}

	void "deployFree() returns 200 and renders a product"() {
		controller.productService = Stub(ProductService) {
			findById(_, _, _) >> product
		}
		controller.productFreeService = Stub(ProductFreeService)

		params.id = "product-id"
		request.method = "POST"
		request.apiUser = new User()
		when:
		withFilters(action: "deployFree") {
			controller.deployFree()
		}
		then:
		response.status == 200
		response.json == product.toMap()
	}

	void "undeployFree() invokes productService#findById (with SHARE permission requirement)"() {
		def productService = controller.productService = Mock(ProductService)
		controller.productFreeService = Stub(ProductFreeService)
		def user = new User()

		params.id = "product-id"
		request.method = "POST"
		request.apiUser = user
		when:
		withFilters(action: "undeployFree") {
			controller.undeployFree()
		}
		then:
		1 * productService.findById('product-id', user, Permission.Operation.PRODUCT_SHARE) >> product
	}

	void "undeployFree() invokes freeProductService#undeployFreeProduct"() {
		controller.productService = Stub(ProductService) {
			findById(_, _, _) >> product
		}
		def freeProductService = controller.productFreeService = Mock(ProductFreeService)

		params.id = "product-id"
		request.method = "POST"
		request.apiUser = new User()
		when:
		withFilters(action: "undeployFree") {
			controller.undeployFree()
		}
		then:
		1 * freeProductService.undeployFreeProduct(product)
	}

	void "undeployFree() returns 200 and renders a product"() {
		controller.productService = Stub(ProductService) {
			findById(_, _, _) >> product
		}
		controller.productFreeService = Stub(ProductFreeService)

		params.id = "product-id"
		request.method = "POST"
		request.apiUser = new User()
		when:
		withFilters(action: "undeployFree") {
			controller.undeployFree()
		}
		then:
		response.status == 200
		response.json == product.toMap()
	}

}
