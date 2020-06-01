package com.unifina.controller.api

import com.google.common.collect.Lists
import com.streamr.client.protocol.message_layer.StreamMessage
import com.streamr.client.protocol.message_layer.StreamMessageV31
import com.unifina.api.*
import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.ApiService
import com.unifina.service.FreeProductService
import com.unifina.service.PermissionService
import com.unifina.service.ProductImageService
import com.unifina.service.ProductService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.web.json.JSONArray
import org.springframework.mock.web.MockMultipartFile
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse

@TestFor(ProductApiController)
@Mock([UnifinaCoreAPIFilters, SecUser])
class ProductApiControllerSpec extends Specification {

	Product product

	void setup() {
		def category = new Category(name: "category")
		category.id = "category-id"
		SecUser user = new SecUser(
			username: "user@domain.com",
			name: "Firstname Lastname",
			password: "salasana"
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

	Product newProduct(String id, String name, Stream... s) {
		Product p = new Product(
			name: name,
			streams: s,
		)
		p.id = id
		return p
	}

	Stream newStream(String id, String name) {
		Stream s = new Stream(name: name)
		s.id = id
		return s
	}

	Date newDate(int minusMilliSeconds) {
		Date d = new Date(System.currentTimeMillis() - minusMilliSeconds)
		return d
	}

	StreamMessage buildMsg(String streamId, int streamPartition, Date timestamp, Map content) {
		return new StreamMessageV31(streamId, streamPartition, timestamp.getTime(), 0, "", "", null, null,
			StreamMessage.ContentType.CONTENT_TYPE_JSON, StreamMessage.EncryptionType.NONE, content, StreamMessage.SignatureType.SIGNATURE_TYPE_NONE, null)
	}

	void "stale products"() {
		setup:
		// Fresh products
		Product a = newProduct("a", "Air quality")
		Product b = newProduct("b", "Wind speed")
		Product d = newProduct("d", "Hacked computers")
		Product e = newProduct("e", "Time machine")

		// Stale streams and products
		// Stale product 1
		Stream s3 = newStream("s3", "Storm Stream") // This stream has a message three days ago
		Product c = newProduct("c", "Storm warning", s3)
		StreamMessage m3 = buildMsg("s3", 1, newDate(3*24*60*60*1000), new HashMap())
		ProductService.StaleProduct sp1 = new ProductService.StaleProduct(c)
		ProductService.StreamWithLatestMessage sm1 = new ProductService.StreamWithLatestMessage(s3, m3)
		sp1.streams.add(sm1)

		// Stale product 2
		Stream s6 = newStream("s6", "Mainframe Stream") // This stream has a message two weeks ago
		StreamMessage m6 = buildMsg("s6", 1, newDate(14*24*60*60*1000), new HashMap())
		Stream s6b = newStream("s6b", "Mainframe B Stream") // This stream has a message one week ago
		StreamMessage m6b = buildMsg("s6b", 1, newDate(7*24*60*60*1000), new HashMap())
		Product f = newProduct("f", "Mainframe connector", s6, s6b)
		ProductService.StaleProduct sp2 = new ProductService.StaleProduct(f)
		ProductService.StreamWithLatestMessage sm2 = new ProductService.StreamWithLatestMessage(s6, m6)
		sp2.streams.add(sm2)
		ProductService.StreamWithLatestMessage sm3 = new ProductService.StreamWithLatestMessage(s6b, m6b)
		sp2.streams.add(sm3)

		// Stale product 3
		Stream s7 = newStream("s7", "Barometer Stream") // This stream doesn't have any messages
		Product g = newProduct("g", "Barometer", s7)
		ProductService.StaleProduct sp3 = new ProductService.StaleProduct(g)
		ProductService.StreamWithLatestMessage sm4 = new ProductService.StreamWithLatestMessage(s7, null)
		sp3.streams.add(sm4)

		// Stale product 4
		Stream s8 = newStream("s8", "Stream a") // This stream has message six days ago
		Stream s9 = newStream("s9", "Stream b") // This stream has message over five days ago
		Product h = newProduct("h", "Product with two streams", s8, s9)
		StreamMessage m8 = buildMsg("s8", 1, newDate(6*24*60*60*1000), new HashMap())
		StreamMessage m9 = buildMsg("s9", 1, newDate(5*25*60*60*1000), new HashMap())
		ProductService.StaleProduct sp4 = new ProductService.StaleProduct(h)
		ProductService.StreamWithLatestMessage sm5 = new ProductService.StreamWithLatestMessage(s8, m8)
		sp4.streams.add(sm5)
		ProductService.StreamWithLatestMessage sm6 = new ProductService.StreamWithLatestMessage(s9, m9)
		sp4.streams.add(sm6)

		controller.productService = Mock(ProductService)
		List<ProductService.StaleProduct> products = Lists.newArrayList(a, b, c, d, e, f, g, h)

		when:
		withFilters(action: "staleProducts") {
			controller.staleProducts()
		}

		then:
		1 * controller.productService.list(_, _) >> products
		1 * controller.productService.findStaleProducts(products, _) >> Lists.newArrayList(sp1, sp2, sp3, sp4)

		JSONArray json = (JSONArray) response.json
		json.size() == 4
		json.find { it.product.id == c.id }
		json.find { it.product.id == f.id }
		json.find { it.product.id == g.id }
		json.find { it.product.id == h.id }
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
			product.toSummaryMap()
		]
	}

	void "show() invokes productService#findById"() {
		controller.permissionService = Mock(PermissionService)
		def productService = controller.productService = Mock(ProductService)

		params.id = "product-id"
		when:
		withFilters(action: "index") {
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
		controller.permissionService = Mock(PermissionService)
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
		1 * controller.permissionService.check(_, _, Permission.Operation.PRODUCT_SHARE) >> false
	}

	void "update() returns 200 and renders a product"() {
		controller.permissionService = Mock(PermissionService)
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
		1 * controller.permissionService.check(_, _, Permission.Operation.PRODUCT_SHARE) >> false
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
		1 * productService.findById("product-id", user, Permission.Operation.PRODUCT_EDIT) >> product
		1 * productService.transitionToDeploying(product)
	}

	void "setDeploying() returns 200 and renders a product"() {
		controller.productService = Stub(ProductService) {
			findById("product-id", _ as SecUser, Permission.Operation.PRODUCT_EDIT) >> product
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
		1 * productService.findById("product-id", user, Permission.Operation.PRODUCT_EDIT) >> product
		1 * productService.transitionToUndeploying(product)
	}

	void "setUndeploying() returns 200 and renders a product"() {
		controller.productService = Stub(ProductService) {
			findById("product-id", _ as SecUser, Permission.Operation.PRODUCT_EDIT) >> product
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

	void "uploadImage() responds with 400 and PARAMETER_MISSING if file not given"() {
		def productService = controller.productService = Mock(ProductService)
		controller.productImageService = Stub(ProductImageService)

		def user = request.apiUser = new SecUser()

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

		def user = request.apiUser = new SecUser()

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
		controller.freeProductService = Stub(FreeProductService)
		def user = new SecUser()

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
		def freeProductService = controller.freeProductService = Mock(FreeProductService)

		params.id = "product-id"
		request.method = "POST"
		request.apiUser = new SecUser()
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
		controller.freeProductService = Stub(FreeProductService)

		params.id = "product-id"
		request.method = "POST"
		request.apiUser = new SecUser()
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
		controller.freeProductService = Stub(FreeProductService)
		def user = new SecUser()

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
		def freeProductService = controller.freeProductService = Mock(FreeProductService)

		params.id = "product-id"
		request.method = "POST"
		request.apiUser = new SecUser()
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
		controller.freeProductService = Stub(FreeProductService)

		params.id = "product-id"
		request.method = "POST"
		request.apiUser = new SecUser()
		when:
		withFilters(action: "undeployFree") {
			controller.undeployFree()
		}
		then:
		response.status == 200
		response.json == product.toMap()
	}

}
