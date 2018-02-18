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
	Category category

	void setup() {
		mockForConstraintsTests(Product)
		category = new Category(name: "Category")
		category.id = "category-id"
		category.save()
	}

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
	void "transitionToDeploying() throws InvalidStateTransitionException if Product.state == #state"(Product.State state) {
		def product = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0x0000000000000000000000000000000000000000",
			pricePerSecond: 10,
			category: category,
			state: state
		)
		when:
		service.transitionToDeploying(product, "0xABCDEF")
		then:
		thrown(InvalidStateTransitionException)
		where:
		state << [Product.State.DEPLOYING, Product.State.DELETING, Product.State.DEPLOYED, Product.State.DELETED]
	}

	void "transitionToDeploying() throws ValidationException given invalid tx"() {
		def product = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0x0000000000000000000000000000000000000000",
			pricePerSecond: 10,
			category: category,
			state: Product.State.NEW
		)
		when:
		service.transitionToDeploying(product, "0x0")
		then:
		thrown(grails.validation.ValidationException)
	}

	void "transitionToDeploying() sets tx and transitions Product from NEW to DEPLOYING"() {
		def product = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0x0000000000000000000000000000000000000000",
			pricePerSecond: 10,
			category: category,
			state: Product.State.NEW
		)
		when:
		service.transitionToDeploying(product, "0x9e37846ea5238d0a630e8710249d3bf9668feabcf39360ea7c8778eec2cda1a2")
		then:
		product.state == Product.State.DEPLOYING
		product.tx == "0x9e37846ea5238d0a630e8710249d3bf9668feabcf39360ea7c8778eec2cda1a2"
	}

	@Unroll
	void "transitionToDeleting() throws InvalidStateTransitionException if Product.state == #state"(Product.State state) {
		def product = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0x0000000000000000000000000000000000000000",
			pricePerSecond: 10,
			category: category,
			state: state
		)
		when:
		service.transitionToDeleting(product)
		then:
		thrown(InvalidStateTransitionException)
		where:
		state << [Product.State.DEPLOYING, Product.State.DELETING, Product.State.DELETED, Product.State.NEW]
	}

	void "transitionToDeleting() transitions Product from DEPLOYED to DELETING"() {
		def product = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0x0000000000000000000000000000000000000000",
			pricePerSecond: 10,
			category: category,
			state: Product.State.DEPLOYED
		)
		when:
		service.transitionToDeleting(product)
		then:
		product.state == Product.State.DELETING
	}

	@Unroll
	void "delete() throws InvalidStateTransitionException if Product.state == #state"(Product.State state) {
		def product = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0x0000000000000000000000000000000000000000",
			pricePerSecond: 10,
			category: category,
			state: state
		)
		when:
		service.delete(product, null)
		then:
		thrown(InvalidStateTransitionException)
		where:
		state << [Product.State.DEPLOYING, Product.State.NEW]
	}

	void "delete() throws NotPermittedException if user is not devops"() {
		def product = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0x0000000000000000000000000000000000000000",
			pricePerSecond: 10,
			category: category,
			state: Product.State.DELETING
		)
		when:
		service.delete(product, Stub(SecUser) {
			isDevOps() >> false
		})
		then:
		thrown(NotPermittedException)
	}

	@Unroll
	void "delete() transitions Product from #state to DELETED"(Product.State state) {
		def product = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0x0000000000000000000000000000000000000000",
			pricePerSecond: 10,
			category: category,
			state: state
		)
		service.permissionService = new PermissionService()

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
		def product = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0x0000000000000000000000000000000000000000",
			pricePerSecond: 10,
			category: category,
			state: Product.State.DELETING
		)
		def permissionService = service.permissionService = Mock(PermissionService)

		when:
		service.delete(product, Stub(SecUser) {
			isDevOps() >> true
		})

		then:
		1 * permissionService.systemRevokeAnonymousAccess(product)
	}
}
