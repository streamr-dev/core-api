package com.unifina.service

import com.google.common.collect.Lists
import com.unifina.api.*
import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.FreeSubscription
import com.unifina.domain.marketplace.PaidSubscription
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.feed.StreamrMessage
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.apache.commons.lang.time.DateUtils
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(ProductService)
@Mock([Category, Product, FreeSubscription, PaidSubscription])
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

	private void setupProduct(Product.State state = Product.State.NOT_DEPLOYED) {
		SecUser user = new SecUser(
			username: "user@domain.com",
			name: "Firstname Lastname",
			password: "salasana"
		)
		user.id = 1
		user.save(failOnError: true, validate: false)
		product = new Product(
				name: "name",
				description: "description",
				ownerAddress: "0x0000000000000000000000000000000000000000",
				beneficiaryAddress: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
				streams: s1 != null ? [s1, s2, s3] : [],
				pricePerSecond: 10,
				category: category,
				state: state,
				blockNumber: 40000,
				blockIndex: 30,
				owner: user
		)
		product.id = "product-id"
		product.save(failOnError: true, validate: true)
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
		return new Date(System.currentTimeMillis() - milliSeconds)
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

		Product a = newProduct("a", "Air quality", s1)// X
		Product b = newProduct("b", "Wind speed", s2)// X
		Product c = newProduct("c", "Storm warning", s3)
		Product d = newProduct("d", "Hacked computers", s4)//X
		Product e = newProduct("e", "Time machine", s5)//X
		Product f = newProduct("f", "Mainframe connector", s6, s6b)
		Product g = newProduct("g", "Barometer", s7)
		Product h = newProduct("h", "Product with two streams", s8, s9)

		StreamrMessage m1 = new StreamrMessage("s1", 1, newDate(4*60*60*1000), new HashMap())
		StreamrMessage m2 = new StreamrMessage("s2", 1, newDate(2*60*1000), new HashMap())
		StreamrMessage m3 = new StreamrMessage("s3", 1, newDate(3*24*60*60*1000), new HashMap())
		StreamrMessage m4 = new StreamrMessage("s4", 1, newDate(24*60*60*1000), new HashMap())
		StreamrMessage m5 = new StreamrMessage("s5", 1, newDate(2*60*60*1000), new HashMap())
		//StreamrMessage m6 = new StreamrMessage("s6", 1, newDate(14*24*60*60*1000), new HashMap())
		StreamrMessage m6b = new StreamrMessage("s6b", 1, newDate(7*24*60*60*1000), new HashMap())
		StreamrMessage m7 = null
		StreamrMessage m8 = new StreamrMessage("s8", 1, newDate(24*60*60*1000), new HashMap())
		StreamrMessage m9 = new StreamrMessage("s9", 1, newDate(28*60*60*1000), new HashMap())

		service.cassandraService = Mock(CassandraService)

		when:
		Date threshold = DateUtils.addDays(new Date(), -2)
		List<ProductService.StaleProduct> results = service.findStaleProducts(Lists.newArrayList(a, b, c, d, e, f, g, h), threshold)

		then:
		1 * service.cassandraService.getLatestFromAllPartitions(s1) >> m1
		1 * service.cassandraService.getLatestFromAllPartitions(s2) >> m2
		1 * service.cassandraService.getLatestFromAllPartitions(s3) >> m3
		1 * service.cassandraService.getLatestFromAllPartitions(s4) >> m4
		1 * service.cassandraService.getLatestFromAllPartitions(s5) >> m5
		1 * service.cassandraService.getLatestFromAllPartitions(s6) >> m6b
		1 * service.cassandraService.getLatestFromAllPartitions(s7) >> m7
		1 * service.cassandraService.getLatestFromAllPartitions(s8) >> m8
		1 * service.cassandraService.getLatestFromAllPartitions(s9) >> m9

		results.size() == 5
		results.find { it.product.id == "a" && it.product.name == "Air quality" && it.streams.size() == 1 /*&& it.streams.get(0).id == "s1"*/ }
		results.find { it.product.id == "b" && it.product.name == "Wind speed" && it.streams.size() == 1 /*&& it.streams.get(0).id == "s2"*/ }
		results.find { it.product.id == "d" && it.product.name == "Hacked computers" && it.streams.size() == 1 /*&& it.streams.get(0).id == "s4"*/ }
		results.find { it.product.id == "e" && it.product.name == "Time machine" && it.streams.size() == 1 /*&& it.streams.get(0).id == "s5"*/ }
		results.find { it.product.id == "h" && it.product.name == "Product with two streams" && it.streams.size() == 2 /*&& it.streams.get(0).id == "s8" && it.streams.get(1).id == "s9"*/ }
	}

	void "list() delegates to ApiService#list"() {
		def apiService = service.apiService = Mock(ApiService)
		def me = new SecUser(username: "me@streamr.com")

		when:
		service.list(new ProductListParams(max: 5), me)

		then:
		1 * apiService.list(Product, {
			assert it.toMap() == new ProductListParams(max: 5, sortBy: "score", order: "desc").toMap()
			true
		}, me)
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
				category: category,
				streams: [s1, s2, s3],
				ownerAddress: "0x0000000000000000000000000000000000000000",
				beneficiaryAddress: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
				pricePerSecond: 10,
				minimumSubscriptionInSeconds: 1
		)

		def user = new SecUser()
		user.name = "Arnold Schwarzenegger"
		when:
		def product = service.create(validCommand, user)

		then:
		Product.findAll() == [product]

		and:
		product.toMap() == [
			id: "1",
			name: "Product",
			description: "Description of Product.",
			imageUrl: null,
			thumbnailUrl: null,
			category: "category-id",
			streams: ["stream-1", "stream-2", "stream-3"],
			state: "NOT_DEPLOYED",
			previewStream: null,
			previewConfigJson: null,
			created: product.dateCreated,
			updated: product.lastUpdated,
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
			pricePerSecond: "10",
			isFree: false,
			priceCurrency: "DATA",
			minimumSubscriptionInSeconds: 1,
			owner: "Arnold Schwarzenegger"
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

	void "create() creates and returns Product when name is empty and description/category are null"() {
		setupStreams()
		service.permissionService = Stub(PermissionService)

		def validCommand = new CreateProductCommand(
			name: "",
			description: null,
			category: null,
			streams: [s1, s2, s3],
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
			pricePerSecond: 10,
			minimumSubscriptionInSeconds: 1
		)
		def user = new SecUser()
		user.name = "Arnold Schwarzenegger"

		when:
		def product = service.create(validCommand, user)

		then:
		Product.findAll() == [product]

		and:
		product.toMap() == [
			id: "1",
			name: "Untitled Product",
			description: null,
			imageUrl: null,
			thumbnailUrl: null,
			category: null,
			streams: ["stream-1", "stream-2", "stream-3"],
			state: "NOT_DEPLOYED",
			previewStream: null,
			previewConfigJson: null,
			created: product.dateCreated,
			updated: product.lastUpdated,
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
			pricePerSecond: "10",
			isFree: false,
			priceCurrency: "DATA",
			minimumSubscriptionInSeconds: 1,
			owner: "Arnold Schwarzenegger"
		]
		product.dateCreated != null
		product.dateCreated == product.lastUpdated
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

		service.subscriptionService = Stub(SubscriptionService)
		service.apiService = Stub(ApiService) {
			authorizedGetById(Product, _, _, _) >> product
		}
		def permissionService = service.permissionService = Mock(PermissionService)

		def validCommand = new UpdateProductCommand(
				name: "updated name",
				description: "updated description",
				category: category,
				streams: [s2, s4],
				pricePerSecond: 20L,
				ownerAddress: "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
				beneficiaryAddress: "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
				priceCurrency: Product.Currency.DATA,
				minimumSubscriptionInSeconds: 1000
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

		service.subscriptionService = Stub(SubscriptionService)
		def apiService = service.apiService = Mock(ApiService)

		def validCommand = new UpdateProductCommand(
				name: "updated name",
				description: "updated description",
				category: category,
				streams: [],
				pricePerSecond: 20L,
				ownerAddress: "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
				beneficiaryAddress: "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
				priceCurrency: Product.Currency.DATA,
				minimumSubscriptionInSeconds: 1000
		)
		def user = new SecUser(username: "me@streamr.com")

		when:
		service.update("product-id", validCommand, user)

		then:
		1 * apiService.authorizedGetById(Product, 'product-id', user, Permission.Operation.WRITE) >> product
	}

	void "update() invokes subscriptionService#afterProductUpdated after Product updated"() {
		setupStreams()
		setupProduct()

		def subscriptionService = service.subscriptionService = Mock(SubscriptionService)
		service.apiService = Stub(ApiService) {
			authorizedGetById(Product, _, _, _) >> product
		}
		service.permissionService = Stub(PermissionService)

		def validCommand = new UpdateProductCommand(
			name: "updated name",
			description: "updated description",
			category: category,
			streams: [s2, s4],
			pricePerSecond: 20L,
			ownerAddress: "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
			beneficiaryAddress: "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
			priceCurrency: Product.Currency.DATA,
			minimumSubscriptionInSeconds: 1000
		)
		def user = new SecUser(username: "me@streamr.com")

		when:
		service.update("product-id", validCommand, user)
		then:
		1 * subscriptionService.afterProductUpdated(product)
	}

	void "update() updates and returns Product with correct info"() {
		setupStreams()
		setupProduct()

		Category category2 = new Category(name: "Category 2")
		category2.id = "category2-id"
		category2.save()

		service.subscriptionService = Stub(SubscriptionService)
		service.apiService = Stub(ApiService) {
			authorizedGetById(Product, _, _, _) >> product
		}
		service.permissionService = Stub(PermissionService)

		def validCommand = new UpdateProductCommand(
				name: "updated name",
				description: "updated description",
				category: category2,
				streams: [s2, s4],
				pricePerSecond: 20L,
				ownerAddress: "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
				beneficiaryAddress: "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
				priceCurrency: Product.Currency.DATA,
				minimumSubscriptionInSeconds: 1000
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
				imageUrl: null,
				thumbnailUrl: null,
				category: "category2-id",
				streams: ["stream-2", "stream-4"],
				state: "NOT_DEPLOYED",
				previewStream: null,
				previewConfigJson: null,
				created: product.dateCreated,
				updated: product.lastUpdated,
				ownerAddress: "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
				beneficiaryAddress: "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
				pricePerSecond: "20",
				isFree: false,
				priceCurrency: "DATA",
				minimumSubscriptionInSeconds: 1000,
				owner: "Firstname Lastname"
		]
		product.dateCreated < product.lastUpdated
	}

	void "addStreamToProduct() verifies Stream via PermissionService#verifyShare"() {
		setupStreams()
		setupProduct()
		service.subscriptionService = Stub(SubscriptionService)
		def permissionService = service.permissionService = Mock(PermissionService)
		def user = new SecUser()
		when:
		service.addStreamToProduct(product, s4, user)
		then:
		1 * permissionService.verifyShare(user, s4)

	}

	void "addStreamToProduct() adds Stream to Product"() {
		setupStreams()
		setupProduct()
		assert !product.streams.contains(s4)

		service.subscriptionService = Stub(SubscriptionService)
		service.permissionService = Stub(PermissionService)
		def user = new SecUser()

		when:
		service.addStreamToProduct(product, s4, user)
		then:
		product.streams.contains(s4)
	}

	void "addStreamToProduct() invokes subscriptionService#afterProductUpdated"() {
		setupStreams()
		setupProduct()
		def subscriptionService = service.subscriptionService = Mock(SubscriptionService)
		service.permissionService = Stub(PermissionService)
		def user = new SecUser()

		when:
		service.addStreamToProduct(product, s4, user)
		then:
		1 * subscriptionService.afterProductUpdated(product)
	}

	void "removeStreamFromProduct() removes Stream from Product"() {
		setupStreams()
		setupProduct()
		service.subscriptionService = Stub(SubscriptionService)
		assert product.streams.contains(s1)

		when:
		service.removeStreamFromProduct(product, s1)
		then:
		!product.streams.contains(s1)
	}

	void "removeStreamFromProduct() invokes subscriptionService#afterProductUpdated"() {
		setupStreams()
		setupProduct()
		def subscriptionService = service.subscriptionService = Mock(SubscriptionService)

		when:
		service.removeStreamFromProduct(product, s1)
		then:
		1 * subscriptionService.afterProductUpdated(product)
	}

	@Unroll
	void "transitionToDeploying() throws InvalidStateTransitionException if Product.state == #state"(Product.State state) {
		setupProduct(state)
		when:
		service.transitionToDeploying(product)
		then:
		thrown(InvalidStateTransitionException)
		where:
		state << [Product.State.DEPLOYING, Product.State.UNDEPLOYING, Product.State.DEPLOYED]
	}

	void "transitionToDeploying() transitions Product from NOT_DEPLOYED to DEPLOYING"() {
		setupProduct(Product.State.NOT_DEPLOYED)
		when:
		service.transitionToDeploying(product)
		then:
		product.state == Product.State.DEPLOYING
	}

	@Unroll
	void "transitionToUndeploying() throws InvalidStateTransitionException if Product.state == #state"(Product.State state) {
		setupProduct(state)
		when:
		service.transitionToUndeploying(product)
		then:
		thrown(InvalidStateTransitionException)
		where:
		state << [Product.State.DEPLOYING, Product.State.UNDEPLOYING, Product.State.NOT_DEPLOYED]
	}

	void "transitionToUndeploying() transitions Product from DEPLOYED to UNDEPLOYING"() {
		setupProduct(Product.State.DEPLOYED)
		when:
		service.transitionToUndeploying(product)
		then:
		product.state == Product.State.UNDEPLOYING
	}

	@Unroll
	void "markAsUndeployed() throws InvalidStateTransitionException if Product.state == #state"(Product.State state) {
		setupProduct(state)
		def command = new ProductUndeployedCommand(blockNumber: 50000, blockIndex: 15)
		when:
		service.markAsUndeployed(product, command, null)
		then:
		thrown(InvalidStateTransitionException)
		where:
		state << [Product.State.DEPLOYING, Product.State.NOT_DEPLOYED]
	}

	void "markAsUndeployed() throws NotPermittedException if user is not devops"() {
		setupProduct(Product.State.UNDEPLOYING)
		def command = new ProductUndeployedCommand(blockNumber: 50000, blockIndex: 15)

		when:
		service.markAsUndeployed(product, command, Stub(SecUser) {
			isDevOps() >> false
		})
		then:
		thrown(NotPermittedException)
	}

	void "markAsUndeployed() returns false if command is stale"() {
		setupProduct(Product.State.UNDEPLOYING)
		service.permissionService = new PermissionService()

		def command = new ProductUndeployedCommand(blockNumber: 30000, blockIndex: 15)

		when:
		boolean result = service.markAsUndeployed(product, command, Stub(SecUser) {
			isDevOps() >> true
		})

		then:
		!result
	}

	void "markAsUndeployed() does not invoke permissionService#systemRevokeAnonymousAccess if command is stale"() {
		setupProduct(Product.State.DEPLOYED)
		def permissionService = service.permissionService = Mock(PermissionService)

		def command = new ProductUndeployedCommand(blockNumber: 30000, blockIndex: 15)

		when:
		service.markAsUndeployed(product, command, Stub(SecUser) {
			isDevOps() >> true
		})

		then:
		0 * permissionService.systemRevokeAnonymousAccess(_)
	}

	void "markAsUndeployed() does not transition Product to NOT_DEPLOYED if command is stale"() {
		setupProduct(Product.State.DEPLOYED)
		def permissionService = service.permissionService = Mock(PermissionService)

		def command = new ProductUndeployedCommand(blockNumber: 30000, blockIndex: 15)

		when:
		service.markAsUndeployed(product, command, Stub(SecUser) {
			isDevOps() >> true
		})

		then:
		product.state != Product.State.NOT_DEPLOYED
	}

	void "markAsUndeployed() returns true if command not stale"() {
		setupProduct(Product.State.UNDEPLOYING)
		service.permissionService = new PermissionService()

		def command = new ProductUndeployedCommand(blockNumber: 50000, blockIndex: 15)

		when:
		boolean result = service.markAsUndeployed(product, command, Stub(SecUser) {
			isDevOps() >> true
		})

		then:
		result
	}

	@Unroll
	void "markAsUndeployed() transitions Product from #state to NOT_DEPLOYED"(Product.State state) {
		setupProduct(state)
		service.permissionService = new PermissionService()

		def command = new ProductUndeployedCommand(blockNumber: 50000, blockIndex: 15)

		when:
		service.markAsUndeployed(product, command, Stub(SecUser) {
			isDevOps() >> true
		})

		then:
		product.state == Product.State.NOT_DEPLOYED

		where:
		state << [Product.State.DEPLOYED, Product.State.UNDEPLOYING]
	}

	void "markAsUndeployed() invokes permissionService#systemRevokeAnonymousAccess"() {
		setupProduct(Product.State.UNDEPLOYING)
		def permissionService = service.permissionService = Mock(PermissionService)

		def command = new ProductUndeployedCommand(blockNumber: 50000, blockIndex: 15)

		when:
		service.markAsUndeployed(product, command, Stub(SecUser) {
			isDevOps() >> true
		})

		then:
		1 * permissionService.systemRevokeAnonymousAccess(product)
	}

	void "markAsDeployed() throws ValidationException if command object does not pass validation"() {
		setupProduct()
		when:
		service.markAsDeployed(product, new ProductDeployedCommand(), new SecUser())
		then:
		thrown(ValidationException)
	}

	void "markAsDeployed() throws InvalidStateTransitionException if Product.state == UNDEPLOYING"() {
		setupProduct(Product.State.UNDEPLOYING)

		def command = new ProductDeployedCommand(
				ownerAddress: "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
				beneficiaryAddress: "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				pricePerSecond: 2,
				priceCurrency: Product.Currency.USD,
				minimumSubscriptionInSeconds: 600,
				blockNumber: 50000,
				blockIndex: 10
		)

		when:
		service.markAsDeployed(product, command, new SecUser())
		then:
		thrown(InvalidStateTransitionException)
	}

	void "markAsDeployed() throws NotPermittedException if user is not devops"() {
		setupProduct()

		def command = new ProductDeployedCommand(
				ownerAddress: "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
				beneficiaryAddress: "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				pricePerSecond: 2,
				priceCurrency: Product.Currency.USD,
				minimumSubscriptionInSeconds: 600,
				blockNumber: 50000,
				blockIndex: 10
		)

		when:
		service.markAsDeployed(product, command, Stub(SecUser) {
			isDevOps() >> false
		})
		then:
		thrown(NotPermittedException)
	}

	void "markAsDeployed() returns false if command object is stale"() {
		setupProduct()
		service.permissionService = Stub(PermissionService)

		def command = new ProductDeployedCommand(
				ownerAddress: "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
				beneficiaryAddress: "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				pricePerSecond: 2,
				priceCurrency: Product.Currency.USD,
				minimumSubscriptionInSeconds: 600,
				blockNumber: 30000,
				blockIndex: 10
		)

		when:
		boolean result = service.markAsDeployed(product, command, Stub(SecUser) {
			isDevOps() >> true
		})

		then:
		!result
	}

	void "markAsDeployed() does not invoke permissionService#systemGrantAnonymousAccess if command object is stale"() {
		setupProduct()
		def permissionService = service.permissionService = Mock(PermissionService)

		def command = new ProductDeployedCommand(
				ownerAddress: "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
				beneficiaryAddress: "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				pricePerSecond: 2,
				priceCurrency: Product.Currency.USD,
				minimumSubscriptionInSeconds: 600,
				blockNumber: 30000,
				blockIndex: 10
		)

		when:
		boolean result = service.markAsDeployed(product, command, Stub(SecUser) {
			isDevOps() >> true
		})

		then:
		0 * permissionService.systemGrantAnonymousAccess(_)
	}

	void "markAsDeployed() does not update Product if command object is stale"() {
		setupProduct()
		service.permissionService = Stub(PermissionService)

		def command = new ProductDeployedCommand(
				ownerAddress: "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
				beneficiaryAddress: "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				pricePerSecond: 2,
				priceCurrency: Product.Currency.USD,
				minimumSubscriptionInSeconds: 600,
				blockNumber: 30000,
				blockIndex: 10
		)

		when:
		service.markAsDeployed(product, command, Stub(SecUser) {
			isDevOps() >> true
		})

		then:
		product.state == Product.State.NOT_DEPLOYED
		product.ownerAddress == "0x0000000000000000000000000000000000000000"
	}

	void "markAsDeployed() returns true if command not stale"() {
		setupProduct()
		service.permissionService = Stub(PermissionService)

		def command = new ProductDeployedCommand(
				ownerAddress: "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
				beneficiaryAddress: "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				pricePerSecond: 2,
				priceCurrency: Product.Currency.USD,
				minimumSubscriptionInSeconds: 600,
				blockNumber: 50000,
				blockIndex: 10
		)

		when:
		boolean result = service.markAsDeployed(product, command, Stub(SecUser) {
			isDevOps() >> true
		})

		then:
		result
	}

	void "markAsDeployed() transitions Product to DEPLOYED and updates Blockchain-related information"() {
		setupProduct()
		service.permissionService = Stub(PermissionService)

		def command = new ProductDeployedCommand(
				ownerAddress: "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
				beneficiaryAddress: "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				pricePerSecond: 2,
				priceCurrency: Product.Currency.USD,
				minimumSubscriptionInSeconds: 600,
				blockNumber: 50000,
				blockIndex: 10
		)

		when:
		service.markAsDeployed(product, command, Stub(SecUser) {
			isDevOps() >> true
		})

		then:
		Product.findById("product-id").toMap() == product.toMap()

		and:
		product.toMap() == [
				id: "product-id",
				name: "name",
				description: "description",
				imageUrl: null,
				thumbnailUrl: null,
				category: "category-id",
				streams: [],
				previewStream: null,
				previewConfigJson: null,
				created: product.dateCreated,
				updated: product.lastUpdated,

				// changes below
				state: "DEPLOYED",
				ownerAddress: "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
				beneficiaryAddress: "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				pricePerSecond: "2",
				isFree: false,
				priceCurrency: "USD",
				minimumSubscriptionInSeconds: 600,
				owner: "Firstname Lastname"
		]
	}

	void "markAsDeployed() grants public access to Product via permissionService#systemGrantAnonymousAccess"() {
		setupProduct()
		def permissionService = service.permissionService = Mock(PermissionService)

		def command = new ProductDeployedCommand(
				ownerAddress: "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
				beneficiaryAddress: "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
				pricePerSecond: 2,
				priceCurrency: Product.Currency.USD,
				minimumSubscriptionInSeconds: 600,
				blockNumber: 50000,
				blockIndex: 10
		)

		when:
		def product = service.markAsDeployed(product, command, Stub(SecUser) {
			isDevOps() >> true
		})

		then:
		1 * permissionService.systemGrantAnonymousAccess(product)
	}

	void "updatePricing() updates product price etc"() {
		setupProduct(Product.State.DEPLOYED)
		service.permissionService = new PermissionService()

		def command = new SetPricingCommand(
			ownerAddress: "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
			beneficiaryAddress: "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
			pricePerSecond: 2,
			priceCurrency: Product.Currency.USD,
			minimumSubscriptionInSeconds: 600,
			blockNumber: 50000,
			blockIndex: 10
		)

		when:
		service.updatePricing(product, command, Stub(SecUser) {
			isDevOps() >> true
		})

		then:
		product.ownerAddress == "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"
		product.beneficiaryAddress == "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
		product.pricePerSecond == 2
		product.priceCurrency == Product.Currency.USD
		product.minimumSubscriptionInSeconds == 600
		product.blockNumber == 50000
		product.blockIndex == 10
	}

	void "updatePricing() throws NotPermittedException if user is not devops"() {
		setupProduct()

		def command = new SetPricingCommand(
			ownerAddress: "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
			beneficiaryAddress: "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
			pricePerSecond: 2,
			priceCurrency: Product.Currency.USD,
			minimumSubscriptionInSeconds: 600,
			blockNumber: 50000,
			blockIndex: 10
		)

		when:
		service.updatePricing(product, command, Stub(SecUser) {
			isDevOps() >> false
		})
		then:
		thrown(NotPermittedException)
	}
}
