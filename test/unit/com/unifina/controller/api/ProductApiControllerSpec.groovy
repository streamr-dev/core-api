package com.unifina.controller.api

import com.google.common.collect.Lists
import com.unifina.api.*
import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.feed.StreamrMessage
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.ApiService
import com.unifina.service.CassandraService
import com.unifina.service.FreeProductService
import com.unifina.service.ProductImageService
import com.unifina.service.ProductService
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonParser
import org.apache.commons.lang.time.DateUtils
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.mock.web.MockMultipartFile
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse
import java.text.SimpleDateFormat

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
			owner: user
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

	Date newDate(int milliSeconds) {
		Date d = new Date(System.currentTimeMillis() - milliSeconds)
		return d
	}

	ProductService.StaleProduct newStaleProduct(Product product, Stream stream, StreamrMessage msg) {
		ProductService.StaleProduct sp = new ProductService.StaleProduct()
		sp.product = product
		ProductService.StreamWithMessages sm = new ProductService.StreamWithMessages()
		sm.stream = stream
		sm.messages.add(msg)
		sp.streams.add(sm)
		return sp
	}

	void "stale products"() {
		setup:
		Stream s1 = newStream("s1","Air Stream") // This stream has a message four hours ago
		Stream s2 = newStream("s2", "Wind Stream") // This stream has a message two minutes ago
		Stream s3 = newStream("s3", "Storm Stream") // This stream has a message three days ago
		Stream s4 = newStream("s4", "Hacked Stream") // This stream has a message one day ago
		Stream s5 = newStream("s5", "Time Stream") // This stream has a message two hours ago
		Stream s6 = newStream("s6", "Mainframe Stream") // This stream has a message two weeks ago
		Stream s6b = newStream("s6b", "Mainframe B Stream") // This stream has a message one week ago
		Stream s7 = newStream("s7", "Barometer Stream") // This stream doesn't have any messages
		Stream s8 = newStream("s8", "Stream a")
		Stream s9 = newStream("s9", "Stream b")

		Product a = newProduct("a", "Air quality", s1)
		Product b = newProduct("b", "Wind speed", s2)
		Product c = newProduct("c", "Storm warning", s3)
		Product d = newProduct("d", "Hacked computers", s4)
		Product e = newProduct("e", "Time machine", s5)
		Product f = newProduct("f", "Mainframe connector", s6, s6b)
		Product g = newProduct("g", "Barometer", s7)
		Product h = newProduct("h", "Product with two streams", s8, s9)

		StreamrMessage m1 = new StreamrMessage("s1", 1, newDate(4*60*60*1000), new HashMap())
		StreamrMessage m2 = new StreamrMessage("s2", 1, newDate(2*60*1000), new HashMap())
		StreamrMessage m4 = new StreamrMessage("s4", 1, newDate(24*60*60*1000), new HashMap())
		StreamrMessage m5 = new StreamrMessage("s5", 1, newDate(2*60*60*1000), new HashMap())
		StreamrMessage m8 = new StreamrMessage("s8", 1, newDate(24*60*60*1000), new HashMap())
		StreamrMessage m9 = new StreamrMessage("s9", 1, newDate(28*60*60*1000), new HashMap())

		ProductService.StaleProduct sp1 = newStaleProduct(a, s1, m1)
		ProductService.StaleProduct sp2 = newStaleProduct(b, s2, m2)
		ProductService.StaleProduct sp3 = newStaleProduct(d, s4, m4)
		ProductService.StaleProduct sp4 = newStaleProduct(e, s5, m5)
		ProductService.StaleProduct sp5 = newStaleProduct(h, s8, m8)
		ProductService.StreamWithMessages sm = new ProductService.StreamWithMessages()
		sm.stream = s9
		sm.messages.add(m9)
		sp5.streams.add(sm)

		controller.productService = Mock(ProductService)
		List<ProductService.StaleProduct> products = Lists.newArrayList(a, b, c, d, e, f, g, h)

		when:
		params.days = 2
		withFilters(action: "stale") {
			controller.staleProducts()
		}

		then:
		1 * controller.productService.list(_, _) >> products
		1 * controller.productService.findStaleProducts(products, _) >> Lists.newArrayList(sp1, sp2, sp3, sp4, sp5)

		JSONArray json = (JSONArray) response.json
		json.size() == 5
		json.find { it.id == "a" && it.name == "Air quality" && it.streams.size() == 1 && it.streams.get(0).id == "s1" }
		json.find { it.id == "b" && it.name == "Wind speed" && it.streams.size() == 1 && it.streams.get(0).id == "s2" }
		json.find { it.id == "d" && it.name == "Hacked computers" && it.streams.size() == 1 && it.streams.get(0).id == "s4" }
		json.find { it.id == "e" && it.name == "Time machine" && it.streams.size() == 1 && it.streams.get(0).id == "s5" }
		json.find { it.id == "h" && it.name == "Product with two streams" && it.streams.size() == 2 && it.streams.get(0).id == "s8" && it.streams.get(1).id == "s9" }

		//System.out.println(response.json)
		for (Object ob : response.json) {
			JSONObject o = (JSONObject) ob
			o.getJSONArray("streams")
			System.out.printf("%s\n", o)
		}
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
		1 * productService.findById("product-id", user, Permission.Operation.WRITE) >> product
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
		1 * productService.findById('product-id', user, Permission.Operation.SHARE) >> product
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
		1 * productService.findById('product-id', user, Permission.Operation.SHARE) >> product
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
