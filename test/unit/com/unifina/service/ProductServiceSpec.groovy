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
@Mock([Category, Product])
class ProductServiceSpec extends Specification {
	Stream s1, s2, s3, s4
	Category category
	Product product

	void setup() {
		mockForConstraintsTests(Product)
		category = new Category(name: "Category")
		category.id = "category-id"
		category.save()
	}

	private void setupStreams() {
		s1 = new Stream(name: "stream-1")
		s2 = new Stream(name: "stream-2")
		s3 = new Stream(name: "stream-3")
		s4 = new Stream(name: "stream-4")
		[s1, s2, s3, s4].eachWithIndex { Stream stream, int i -> stream.id = "stream-${i+1}" } // assign ids
		[s1, s2, s3, s4]*.save(failOnError: true, validate: false)
	}

	private void setupProduct(Product.State state = Product.State.NEW) {
		product = new Product(
				name: "name",
				description: "description",
				ownerAddress: "0x0000000000000000000000000000000000000000",
				beneficiaryAddress: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
				streams: s1 != null ? [s1, s2, s3] : [],
				pricePerSecond: 10,
				category: category,
				state: state
		)
		product.id = "product-id"
		product.save(failOnError: true, validate: true)
	}

	void "list() delegates to ApiService#list"() {
		def apiService = service.apiService = Mock(ApiService)
		def me = new SecUser(username: "me@streamr.com")

		when:
		service.list(new ProductListParams(max: 5), me)

		then:
		1 * apiService.list(Product, { it.toMap() == new ProductListParams(max: 5).toMap() }, me)
	}

	void "findById() delegates to ApiService#authorizedGetById"() {
		def apiService = service.apiService = Mock(ApiService)
		def me = new SecUser(username: "me@streamr.com")

		when:
		service.findById("product-id", me, Permission.Operation.READ)

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
		setupStreams()
		service.permissionService = Stub(PermissionService)

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
		setupStreams()
		def permissionService = service.permissionService = Mock(PermissionService)

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

		when:
		service.create(validCommand, me)
		then:
		1 * permissionService.systemGrantAll(me, _ as Product)
	}

	void "create() verifies streams via permissionService#verifyShare"() {
		setupStreams()
		def permissionService = service.permissionService = Mock(PermissionService)

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

		when:
		service.create(validCommand, me)
		then:
		1 * permissionService.verifyShare(me, s1)
		1 * permissionService.verifyShare(me, s2)
		1 * permissionService.verifyShare(me, s3)
	}

	void "create() does not save if permissionService#verifyShare throws"() {
		setupStreams()
		service.permissionService = new PermissionService()

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

		when:
		service.create(validCommand, me)

		then:
		thrown(NotPermittedException)
		Product.count() == 0
	}

	void "update() throws ValidationException if command object does not pass validation"() {
		when:
		service.update("product-id", new UpdateProductCommand(), new SecUser())
		then:
		thrown(ValidationException)
	}

	void "update() verifies streams via permissionService#verifyShare"() {
		setupStreams()
		setupProduct()
		service.apiService = Stub(ApiService) {
			authorizedGetById(Product, _, _, _) >> product
		}
		def permissionService = service.permissionService = Mock(PermissionService)

		def validCommand = new UpdateProductCommand(
				name: "updated name",
				description: "updated description",
				imageUrl: "product.png",
				category: category,
				streams: [s2, s4]
		)
		def user = new SecUser(username: "me@streamr.com")

		when:
		service.update("product-id", validCommand, user)
		then:
		1 * permissionService.verifyShare(user, s2)
		1 * permissionService.verifyShare(user, s4)
	}

	void "update() does not save if permissionService#verifyShare throws"() {
		setupStreams()
		setupProduct()
		service.permissionService = new PermissionService()

		def validCommand = new UpdateProductCommand(
				name: "updated name",
				description: "updated description",
				imageUrl: "product.png",
				category: category,
				streams: [s2, s4]
		)

		when:
		service.update("product-id", validCommand, new SecUser())

		then:
		thrown(NotPermittedException)
		Product.findById("product-id").name == "name"
	}

	void "update() invokes ApiService#authorizedGetById"() {
		setupProduct()
		def apiService = service.apiService = Mock(ApiService)

		def validCommand = new UpdateProductCommand(
				name: "updated name",
				description: "updated description",
				imageUrl: "product.png",
				category: category,
				streams: []
		)
		def user = new SecUser(username: "me@streamr.com")

		when:
		service.update("product-id", validCommand, user)

		then:
		1 * apiService.authorizedGetById(Product, 'product-id', user, Permission.Operation.WRITE) >> product
	}

	void "update() updates and returns Product with correct info"() {
		setupStreams()
		setupProduct()

		Category category2 = new Category(name: "Category 2")
		category2.id = "category2-id"
		category2.save()

		service.apiService = Stub(ApiService) {
			authorizedGetById(Product, _, _, _) >> product
		}
		service.permissionService = Stub(PermissionService)

		def validCommand = new UpdateProductCommand(
				name: "updated name",
				description: "updated description",
				imageUrl: "product.png",
				category: category2,
				streams: [s2, s4]
		)

		when:
		def updatedProduct = service.update("product-id", validCommand, new SecUser())

		then:
		Product.findById("product-id").toMap() == updatedProduct.toMap()

		and:
		updatedProduct.toMap() == [
				id: "product-id",
				name: "updated name",
				description: "updated description",
				imageUrl: "product.png",
				category: "category2-id",
				streams: ["stream-2", "stream-4"],
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
				minimumSubscriptionInSeconds: 0
		]
		product.dateCreated < product.lastUpdated
	}

	@Unroll
	void "transitionToDeploying() throws InvalidStateTransitionException if Product.state == #state"(Product.State state) {
		setupProduct(state)
		when:
		service.transitionToDeploying(product, "0xABCDEF")
		then:
		thrown(InvalidStateTransitionException)
		where:
		state << [Product.State.DEPLOYING, Product.State.DELETING, Product.State.DEPLOYED, Product.State.DELETED]
	}

	void "transitionToDeploying() throws ValidationException given invalid tx"() {
		setupProduct(Product.State.NEW)
		when:
		service.transitionToDeploying(product, "0x0")
		then:
		thrown(grails.validation.ValidationException)
	}

	void "transitionToDeploying() sets tx and transitions Product from NEW to DEPLOYING"() {
		setupProduct(Product.State.NEW)
		when:
		service.transitionToDeploying(product, "0x9e37846ea5238d0a630e8710249d3bf9668feabcf39360ea7c8778eec2cda1a2")
		then:
		product.state == Product.State.DEPLOYING
		product.tx == "0x9e37846ea5238d0a630e8710249d3bf9668feabcf39360ea7c8778eec2cda1a2"
	}

	@Unroll
	void "transitionToDeleting() throws InvalidStateTransitionException if Product.state == #state"(Product.State state) {
		setupProduct(state)
		when:
		service.transitionToDeleting(product)
		then:
		thrown(InvalidStateTransitionException)
		where:
		state << [Product.State.DEPLOYING, Product.State.DELETING, Product.State.DELETED, Product.State.NEW]
	}

	void "transitionToDeleting() transitions Product from DEPLOYED to DELETING"() {
		setupProduct(Product.State.DEPLOYED)
		when:
		service.transitionToDeleting(product)
		then:
		product.state == Product.State.DELETING
	}

	@Unroll
	void "delete() throws InvalidStateTransitionException if Product.state == #state"(Product.State state) {
		setupProduct(state)
		when:
		service.delete(product, null)
		then:
		thrown(InvalidStateTransitionException)
		where:
		state << [Product.State.DEPLOYING, Product.State.NEW]
	}

	void "delete() throws NotPermittedException if user is not devops"() {
		setupProduct(Product.State.DELETING)
		when:
		service.delete(product, Stub(SecUser) {
			isDevOps() >> false
		})
		then:
		thrown(NotPermittedException)
	}

	@Unroll
	void "delete() transitions Product from #state to DELETED"(Product.State state) {
		setupProduct(state)
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
		setupProduct(Product.State.DELETING)
		def permissionService = service.permissionService = Mock(PermissionService)

		when:
		service.delete(product, Stub(SecUser) {
			isDevOps() >> true
		})

		then:
		1 * permissionService.systemRevokeAnonymousAccess(product)
	}
}
