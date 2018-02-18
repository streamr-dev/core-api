package com.unifina.service

import com.unifina.api.*
import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(ProductService)
@Mock([Category])
class ProductServiceSpec extends Specification {
	void "list() delegates to ApiService#list"() {
		def apiService = service.apiService = Mock(ApiService)
		def me = new SecUser(username: "me@streamr.com")

		when:
		service.list(new ProductListParams(max: 5), me)

		then:
		1 * apiService.list(Product, { it.toMap() == new ProductListParams(max: 5).toMap() }, me)
	}

	void "show() delegates to PermissionService#authorizedGetById"() {
		def apiService = service.apiService = Mock(ApiService)
		def me = new SecUser(username: "me@streamr.com")

		when:
		service.findById("product-id", me)

		then:
		1 * apiService.authorizedGetById(Product, "product-id", me, Permission.Operation.READ)
	}

	void "create() throws ValidationException if command object does not pass validation"() {
		when:
		service.create(new CreateProductCommand(), new SecUser())
		then:
		thrown(ValidationException)
	}

	void "create() creates and returns Product with correct info and NEW state"() {
		def category = new Category(name: "Category")
		category.id = "category-id"
		category.save()

		Stream s1 = new Stream(name: "stream-1")
		Stream s2 = new Stream(name: "stream-2")
		Stream s3 = new Stream(name: "stream-3")
		[s1, s2, s3].eachWithIndex { Stream stream, int i -> stream.id = "stream-${i+1}" } // assign ids
		[s1, s2, s3]*.save(failOnError: true, validate: false)

		def validCommand = new CreateProductCommand(
			name: "Product",
			description: "Description of Product.",
			imageUrl: "product.png",
			category: category,
			streams: [s1, s2, s3],
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
			pricePerSecond: 10,
			minimumSubscriptionInSeconds: 1
		)

		service.permissionService = Stub(PermissionService)

		when:
		def product = service.create(validCommand, new SecUser())

		then:
		Product.findAll() == [product]

		and:
		product.toMap() == [
			id: "1",
			name: "Product",
			description: "Description of Product.",
			imageUrl: "product.png",
			category: "category-id",
			streams: ["stream-1", "stream-2", "stream-3"],
			state: "NEW",
			tx: null,
			previewStream: null,
			previewConfigJson: null,
			created: product.dateCreated,
			updated: product.lastUpdated,
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
			pricePerSecond: 10,
			priceCurrency: "DATA",
			minimumSubscriptionInSeconds: 1
		]
		product.dateCreated != null
		product.dateCreated == product.lastUpdated
	}

	void "create() invokes permissionService#systemGrant"() {
		def category = new Category(name: "Category")
		category.id = "category-id"
		category.save()

		Stream s1 = new Stream(name: "stream-1")
		Stream s2 = new Stream(name: "stream-2")
		Stream s3 = new Stream(name: "stream-3")
		[s1, s2, s3].eachWithIndex { Stream stream, int i -> stream.id = "stream-${i+1}" } // assign ids
		[s1, s2, s3]*.save(failOnError: true, validate: false)

		def validCommand = new CreateProductCommand(
			name: "Product",
			description: "Description of Product.",
			imageUrl: "product.png",
			category: category,
			streams: [s1, s2, s3],
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
			pricePerSecond: 10,
			minimumSubscriptionInSeconds: 1
		)
		def me = new SecUser(username: "me@streamr.com")

		def permissionService = service.permissionService = Mock(PermissionService)

		when:
		service.create(validCommand, me)
		then:
		1 * permissionService.systemGrantAll(me, _ as Product)
	}

	void "create() given streams are verified with permissionService#verifyShare"() {
		def category = new Category(name: "Category")
		category.id = "category-id"
		category.save()

		Stream s1 = new Stream(name: "stream-1")
		Stream s2 = new Stream(name: "stream-2")
		Stream s3 = new Stream(name: "stream-3")
		[s1, s2, s3].eachWithIndex { Stream stream, int i -> stream.id = "stream-${i+1}" } // assign ids
		[s1, s2, s3]*.save(failOnError: true, validate: false)

		def validCommand = new CreateProductCommand(
			name: "Product",
			description: "Description of Product.",
			imageUrl: "product.png",
			category: category,
			streams: [s1, s2, s3],
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
			pricePerSecond: 10,
			minimumSubscriptionInSeconds: 1
		)
		def me = new SecUser(username: "me@streamr.com")

		def permissionService = service.permissionService = Mock(PermissionService)

		when:
		service.create(validCommand, me)
		then:
		1 * permissionService.verifyShare(me, s1)
		1 * permissionService.verifyShare(me, s2)
		1 * permissionService.verifyShare(me, s3)
	}

	void "create() does not save if permissionService#verifyShare throws"() {
		def category = new Category(name: "Category")
		category.id = "category-id"
		category.save()

		Stream s1 = new Stream(name: "stream-1")
		Stream s2 = new Stream(name: "stream-2")
		Stream s3 = new Stream(name: "stream-3")
		[s1, s2, s3].eachWithIndex { Stream stream, int i -> stream.id = "stream-${i+1}" } // assign ids
		[s1, s2, s3]*.save(failOnError: true, validate: false)

		def validCommand = new CreateProductCommand(
			name: "Product",
			description: "Description of Product.",
			imageUrl: "product.png",
			category: category,
			streams: [s1, s2, s3],
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
			pricePerSecond: 10,
			minimumSubscriptionInSeconds: 1
		)
		def me = new SecUser(username: "me@streamr.com")

		service.permissionService = new PermissionService()

		when:
		service.create(validCommand, me)

		then:
		thrown(NotPermittedException)
		Product.count() == 0
	}

	@Unroll
	void "delete() throws InvalidStateTransitionException if Product.state == #state"(Product.State state) {
		when:
		service.delete(new Product(state: state), null)
		then:
		thrown(InvalidStateTransitionException)
		where:
		state << [Product.State.DEPLOYING, Product.State.NEW]
	}

	void "delete() throws NotPermittedException if user is not devops"() {
		when:
		service.delete(new Product(state: Product.State.DELETING), Stub(SecUser) {
			isDevOps() >> false
		})
		then:
		thrown(NotPermittedException)
	}

	@Unroll
	void "delete() transitions Product from #state to DELETED"(Product.State state) {
		service.permissionService = new PermissionService()
		def product = new Product(state: state)

		when:
		service.delete(product, Stub(SecUser) {
			isDevOps() >> true
		})

		then:
		product.state == Product.State.DELETED

		where:
		state << [Product.State.DEPLOYED, Product.State.DELETING, Product.State.DELETED]
	}

	void "delete() invokes permissionService#systemRevokeAnonymousAccess"() {
		def permissionService = service.permissionService = Mock(PermissionService)
		def product = new Product(state: Product.State.DELETING)

		when:
		service.delete(product, Stub(SecUser) {
			isDevOps() >> true
		})

		then:
		1 * permissionService.systemRevokeAnonymousAccess(product)
	}
}
