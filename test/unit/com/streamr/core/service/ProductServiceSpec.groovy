package com.streamr.core.service

import com.streamr.core.domain.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(ProductService)
@Mock([Category, Product, User])
class ProductServiceSpec extends Specification {
	String s1, s2, s3, s4
	Category category
	Product product
	Product freeProduct

	void setup() {
		mockForConstraintsTests(Product)
		category = new Category(name: "Category")
		category.id = "category-id"
		category.save()
	}

	private void setupStreams() {
		s1 = "0x0000000000000000000000000000000000000001/abc"
		s2 = "0x0000000000000000000000000000000000000002/def"
		s3 = "0x0000000000000000000000000000000000000003/ghi"
		s4 = "0x0000000000000000000000000000000000000004/jkl"
	}

	private void setupProduct(Product.State state = Product.State.NOT_DEPLOYED) {
		User user = new User(
			username: "0x0000000000000000000000000000000000000123",
			name: "Firstname Lastname",
		)
		user.id = 1
		user.save(failOnError: true, validate: false)
		product = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
			streams: s1 != null ? [s1, s2, s3] : [],
			pricePerSecond: "10",
			category: category,
			state: state,
			blockNumber: 40000,
			blockIndex: 30,
			owner: user
		)
		product.id = "product-id"
		product.save(failOnError: true, validate: true)
	}

	private void setupFreeProduct(Product.State state = Product.State.NOT_DEPLOYED) {
		User user = new User(
			username: "0x0000000000000000000000000000000000001234",
			name: "Firstname Lastname",
		)
		user.id = 1
		user.save(failOnError: true, validate: false)
		freeProduct = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
			streams: s1 != null ? [s1, s2, s3] : [],
			pricePerSecond: "0",
			category: category,
			state: state,
			blockNumber: 40000,
			blockIndex: 30,
			owner: user
		)
		freeProduct.id = "free-product-id"
		freeProduct.save(failOnError: true, validate: true)
	}

	void "list() delegates to ApiService#list"() {
		setup:
		service.apiService = Mock(ApiService)
		def me = new User(username: "0x0000000000000000000000000000000000002341")

		when:
		service.list(new ProductListParams(max: 5), me)

		then:
		1 * service.apiService.list({
			assert it.toMap() == new ProductListParams(max: 5, sortBy: "score", order: "desc").toMap()
			true
		}, me)
	}

	void "findById() delegates to ApiService#authorizedGetById"() {
		setup:
		service.apiService = Mock(ApiService)
		def me = new User(username: "0x0000000000000000000000000000000098762341")

		when:
		service.findById("product-id", me, Permission.Operation.PRODUCT_GET)

		then:
		1 * service.apiService.authorizedGetById("product-id", me, Permission.Operation.PRODUCT_GET)
	}

	void "create() throws ValidationException if command object does not pass validation"() {
		when:
		service.create(new ProductCreateCommand(pricePerSecond: "-1"), new User())
		then:
		thrown(ValidationException)
	}

	void "create() creates and returns Product with correct info and NOT_DEPLOYED state"() {
		setup:
		setupStreams()
		service.permissionService = Mock(PermissionService)

		Contact contact = new Contact()
		contact.url = "https://www.fi"
		TermsOfUse termsOfUse = new TermsOfUse()
		termsOfUse.termsName = "terms link name"
		def validCommand = new ProductCreateCommand(
			name: "Product",
			description: "Description of Product.",
			category: category,
			streams: [s1, s2, s3],
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
			pricePerSecond: "10",
			minimumSubscriptionInSeconds: 1,
			contact: contact,
			termsOfUse: termsOfUse,
		)

		def user = new User()
		user.name = "Arnold Schwarzenegger"
		when:
		def product = service.create(validCommand, user)

		then:
		Product.findAll() == [product]

		and:
		def map = product.toMap()
		map.id == "1"
		map.type == "NORMAL"
		map.name == "Product"
		map.description == "Description of Product."
		map.imageUrl == null
		map.thumbnailUrl == null
		map.category == "category-id"
		map.streams == [s1, s2, s3]
		map.state == "NOT_DEPLOYED"
		map.previewStreamId == null
		map.previewConfigJson == null
		map.created == product.dateCreated
		map.updated == product.lastUpdated
		map.ownerAddress == "0x0000000000000000000000000000000000000000"
		map.beneficiaryAddress == "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
		map.pricePerSecond == "10"
		map.isFree == false
		map.priceCurrency == "DATA"
		map.minimumSubscriptionInSeconds == 1
		map.owner == "Arnold Schwarzenegger"
		map.contact.url == "https://www.fi"
		map.termsOfUse.termsName == "terms link name"
		product.dateCreated != null
		product.dateCreated == product.lastUpdated
	}

	void "create() invokes permissionService#systemGrant"() {
		setup:
		setupStreams()
		service.permissionService = Mock(PermissionService)

		def validCommand = new ProductCreateCommand(
			name: "Product",
			description: "Description of Product.",
			category: category,
			streams: [s1, s2, s3],
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
			pricePerSecond: "10",
			minimumSubscriptionInSeconds: 1
		)
		def me = new User(username: "0x711241f99Aef8D2AFf3e4c5196C8F1e8F4168C93")

		when:
		service.create(validCommand, me)
		then:
		1 * service.permissionService.systemGrantAll(me, _ as Product)
		1 * service.permissionService.verify(_, me.username, s1, StreamPermission.GRANT)
		1 * service.permissionService.verify(_, me.username, s2, StreamPermission.GRANT)
		1 * service.permissionService.verify(_, me.username, s3, StreamPermission.GRANT)
		0 * service.permissionService._
	}

	void "create() with an empty command object creates a product with default values"() {
		setup:
		setupStreams()
		service.permissionService = Stub(PermissionService)

		def validCommand = new ProductCreateCommand()
		def user = new User()
		user.name = "Arnold Schwarzenegger"

		when:
		def product = service.create(validCommand, user)

		then:
		Product.findAll() == [product]

		and:
		def map = product.toMap()
		map.id == "1"
		map.type == "NORMAL"
		map.name == "Untitled Product"
		map.description == null
		map.imageUrl == null
		map.thumbnailUrl == null
		map.category == null
		map.streams == []
		map.state == "NOT_DEPLOYED"
		map.previewStreamId == null
		map.previewConfigJson == null
		map.created == product.dateCreated
		map.updated == product.lastUpdated
		map.ownerAddress == null
		map.beneficiaryAddress == null
		map.pricePerSecond == "0"
		map.isFree == true
		map.priceCurrency == "DATA"
		map.minimumSubscriptionInSeconds == 0
		map.owner == "Arnold Schwarzenegger"
		def c = map.contact
		c.email == null
		c.url == null
		c.social1 == null
		c.social2 == null
		c.social3 == null
		c.social4 == null
		def t = map.termsOfUse
		t.redistribution == true
		t.commercialUse == true
		t.reselling == true
		t.storage == true
		t.termsUrl == null
		t.termsName == null
		product.dateCreated != null
		product.dateCreated == product.lastUpdated
	}

	void "create() can create data unions"() {
		setup:
		setupStreams()
		service.permissionService = Stub(PermissionService)

		def validCommand = new ProductCreateCommand(type: "DATAUNION")
		def user = new User()
		user.name = "Arnold Schwarzenegger"

		when:
		def product = service.create(validCommand, user)

		then:
		Product.findAll() == [product]

		and:
		product.toMap().type == "DATAUNION"
	}

	void "update() allows product chain to be changed before publish"() {
		setup:
		setupProduct()

		service.subscriptionService = Stub(SubscriptionService)
		service.apiService = Mock(ApiService)

		def validCommand = new ProductUpdateCommand(
			name: "updated name",
			description: "updated description",
			category: category,
			streams: [],
			pricePerSecond: "20",
			ownerAddress: "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
			beneficiaryAddress: "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
			priceCurrency: Product.Currency.DATA,
			minimumSubscriptionInSeconds: 1000,
			chain: Product.Chain.AVALANCHE,
		)
		def user = new User(username: "0x0000000000000000000000000000000000000001")

		when:
		service.update("product-id", validCommand, user)

		then:
		1 * service.apiService.authorizedGetById('product-id', user, Permission.Operation.PRODUCT_EDIT) >> product
		product.chain == Product.Chain.AVALANCHE
	}

	void "update() doesnt update chain field if product is published and chain is changed"() {
		setup:
		setupProduct()
		product.writtenToChain = true
		product.save()

		service.subscriptionService = Stub(SubscriptionService)
		service.apiService = Mock(ApiService)

		def validCommand = new ProductUpdateCommand(
			name: "updated name",
			description: "updated description",
			category: category,
			streams: [],
			pricePerSecond: "20",
			ownerAddress: "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
			beneficiaryAddress: "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
			priceCurrency: Product.Currency.DATA,
			minimumSubscriptionInSeconds: 1000,
			chain: Product.Chain.AVALANCHE,
		)
		def user = new User(username: "0x0000000000000000000000000000000000000001")

		when:
		service.update("product-id", validCommand, user)

		then:
		1 * service.apiService.authorizedGetById('product-id', user, Permission.Operation.PRODUCT_EDIT) >> product
		product.chain == Product.Chain.ETHEREUM
	}

	void "update() throws ValidationException if command object does not pass validation"() {
		when:
		service.update("product-id", new ProductUpdateCommand(), new User())
		then:
		thrown(ValidationException)
	}

	void "update() invokes ApiService#authorizedGetById"() {
		setup:
		setupProduct()

		service.subscriptionService = Stub(SubscriptionService)
		service.apiService = Mock(ApiService)

		def validCommand = new ProductUpdateCommand(
			name: "updated name",
			description: "updated description",
			category: category,
			streams: [],
			pricePerSecond: "20",
			ownerAddress: "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
			beneficiaryAddress: "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
			priceCurrency: Product.Currency.DATA,
			minimumSubscriptionInSeconds: 1000,
			chain: Product.Chain.ETHEREUM,
		)
		def user = new User(username: "0x0000000000000000000000000000000000000001")

		when:
		service.update("product-id", validCommand, user)

		then:
		1 * service.apiService.authorizedGetById('product-id', user, Permission.Operation.PRODUCT_EDIT) >> product
	}

	void "update() invokes subscriptionService#afterProductUpdated after Product updated"() {
		setup:
		setupStreams()
		setupProduct()

		service.subscriptionService = Mock(SubscriptionService)
		service.apiService = Stub(ApiService) {
			authorizedGetById(_, _, _) >> product
		}
		service.permissionService = Stub(PermissionService)

		def validCommand = new ProductUpdateCommand(
			name: "updated name",
			description: "updated description",
			category: category,
			streams: [s2, s4],
			pricePerSecond: "20",
			ownerAddress: "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
			beneficiaryAddress: "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
			priceCurrency: Product.Currency.DATA,
			minimumSubscriptionInSeconds: 1000,
			chain: Product.Chain.ETHEREUM,
		)
		def user = new User(username: "me@streamr.network")

		when:
		service.update("product-id", validCommand, user)
		then:
		1 * service.subscriptionService.afterProductUpdated(product)
	}

	void "update() updates and returns Product with correct info"() {
		setup:
		setupStreams()
		setupProduct()

		Category category2 = new Category(name: "Category 2")
		category2.id = "category2-id"
		category2.save()

		service.subscriptionService = Stub(SubscriptionService)
		service.apiService = Stub(ApiService) {
			authorizedGetById(_, _, _) >> product
		}
		service.permissionService = Stub(PermissionService)

		def contact = new Contact()
		contact.email = "email@address.org"
		contact.url = "https://site.com"
		contact.social1 = "https://twitter.com"
		contact.social2 = "https://facebook.com"
		contact.social3 = "https://telegram.com"
		contact.social4 = "https://linkedin.com"

		def terms = new TermsOfUse()
		terms.redistribution = false
		terms.commercialUse = false
		terms.reselling = false
		terms.storage = false
		terms.termsUrl = "https://www.site.org"
		terms.termsName = "legal terms for site.org"

		def validCommand = new ProductUpdateCommand(
			name: "updated name",
			description: "updated description",
			category: category2,
			streams: [s2, s4],
			pricePerSecond: "20",
			ownerAddress: "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
			beneficiaryAddress: "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
			priceCurrency: Product.Currency.DATA,
			minimumSubscriptionInSeconds: 1000,
			contact: contact,
			termsOfUse: terms,
			chain: Product.Chain.ETHEREUM,
		)
		product.writtenToChain = true

		when:
		def updatedProduct = service.update("product-id", validCommand, new User())

		then:
		Product.findById("product-id").toMap() == updatedProduct.toMap()

		and:
		def map = updatedProduct.toMap()
		map.id == "product-id"
		map.type == "NORMAL"
		map.name == "updated name"
		map.description == "updated description"
		map.imageUrl == null
		map.thumbnailUrl == null
		map.category == "category2-id"
		map.streams == [
			"0x0000000000000000000000000000000000000002/def",
			"0x0000000000000000000000000000000000000004/jkl",
		]
		map.state == "NOT_DEPLOYED"
		map.previewStreamId == null
		map.previewConfigJson == null
		map.created == product.dateCreated
		map.updated == product.lastUpdated
		map.ownerAddress == "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC"
		map.beneficiaryAddress == "0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC"
		map.pricePerSecond == "20"
		map.isFree == false
		map.priceCurrency == "DATA"
		map.minimumSubscriptionInSeconds == 1000
		map.owner == "Firstname Lastname"
		product.dateCreated < product.lastUpdated

		def c = map.contact
		c.email == "email@address.org"
		c.url == "https://site.com"
		c.social1 == "https://twitter.com"
		c.social2 == "https://facebook.com"
		c.social3 == "https://telegram.com"
		c.social4 == "https://linkedin.com"

		def t = map.termsOfUse
		t.redistribution == false
		t.commercialUse == false
		t.reselling == false
		t.storage == false
		t.termsUrl == "https://www.site.org"
		t.termsName == "legal terms for site.org"
	}

	void "addStreamToProduct() adds Stream to Product"() {
		setup:
		setupStreams()
		setupProduct()
		assert !product.streams.contains(s4)

		service.subscriptionService = Stub(SubscriptionService)
		service.permissionService = Stub(PermissionService)
		def user = new User()

		when:
		service.addStreamToProduct(product, s4, user)
		then:
		product.streams.contains(s4)
	}

	void "addStreamToProduct() invokes subscriptionService#afterProductUpdated"() {
		setup:
		setupStreams()
		setupProduct()
		service.subscriptionService = Mock(SubscriptionService)
		service.permissionService = Stub(PermissionService)
		def user = new User()

		when:
		service.addStreamToProduct(product, s4, user)
		then:
		1 * service.subscriptionService.afterProductUpdated(product)
	}

	void "removeStreamFromProduct() removes Stream from Product"() {
		setup:
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
		setup:
		setupStreams()
		setupProduct()
		service.subscriptionService = Mock(SubscriptionService)

		when:
		service.removeStreamFromProduct(product, s1)
		then:
		1 * service.subscriptionService.afterProductUpdated(product)
	}

	@Unroll
	void "transitionToDeploying() throws InvalidStateTransitionException if Product.state == #state"(Product.State state) {
		setup:
		setupProduct(state)
		when:
		service.transitionToDeploying(product)
		then:
		thrown(InvalidStateTransitionException)
		where:
		state << [Product.State.DEPLOYING, Product.State.UNDEPLOYING, Product.State.DEPLOYED]
	}

	void "transitionToDeploying() transitions Product from NOT_DEPLOYED to DEPLOYING"() {
		setup:
		setupProduct(Product.State.NOT_DEPLOYED)
		when:
		service.transitionToDeploying(product)
		then:
		product.state == Product.State.DEPLOYING
	}

	@Unroll
	void "transitionToUndeploying() throws InvalidStateTransitionException if Product.state == #state"(Product.State state) {
		setup:
		setupProduct(state)
		when:
		service.transitionToUndeploying(product)
		then:
		thrown(InvalidStateTransitionException)
		where:
		state << [Product.State.DEPLOYING, Product.State.UNDEPLOYING, Product.State.NOT_DEPLOYED]
	}

	void "transitionToUndeploying() transitions Product from DEPLOYED to UNDEPLOYING"() {
		setup:
		setupProduct(Product.State.DEPLOYED)
		when:
		service.transitionToUndeploying(product)
		then:
		product.state == Product.State.UNDEPLOYING
	}

	@Unroll
	void "markAsUndeployed() throws InvalidStateTransitionException if Product.state == #state"(Product.State state) {
		setup:
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
		setup:
		setupProduct(Product.State.UNDEPLOYING)
		def command = new ProductUndeployedCommand(blockNumber: 50000, blockIndex: 15)

		when:
		service.markAsUndeployed(product, command, Stub(User) {
			isDevOps() >> false
		})
		then:
		thrown(NotPermittedException)
	}

	void "markAsUndeployed() returns false if command is stale"() {
		setup:
		setupProduct(Product.State.UNDEPLOYING)
		service.permissionService = new PermissionService()

		def command = new ProductUndeployedCommand(blockNumber: 30000, blockIndex: 15)

		when:
		boolean result = service.markAsUndeployed(product, command, Stub(User) {
			isDevOps() >> true
		})

		then:
		!result
	}

	void "markAsUndeployed() does not invoke permissionService#systemRevokeAnonymousAccess if command is stale"() {
		setup:
		setupProduct(Product.State.DEPLOYED)
		service.permissionService = Mock(PermissionService)

		def command = new ProductUndeployedCommand(blockNumber: 30000, blockIndex: 15)

		when:
		service.markAsUndeployed(product, command, Stub(User) {
			isDevOps() >> true
		})

		then:
		0 * service.permissionService.systemRevokeAnonymousAccess(_)
	}

	void "markAsUndeployed() does not transition Product to NOT_DEPLOYED if command is stale"() {
		setup:
		setupProduct(Product.State.DEPLOYED)

		def command = new ProductUndeployedCommand(blockNumber: 30000, blockIndex: 15)

		when:
		service.markAsUndeployed(product, command, Stub(User) {
			isDevOps() >> true
		})

		then:
		product.state != Product.State.NOT_DEPLOYED
	}

	void "markAsUndeployed() returns true if command not stale"() {
		setup:
		setupProduct(Product.State.UNDEPLOYING)
		service.permissionService = new PermissionService()

		def command = new ProductUndeployedCommand(blockNumber: 50000, blockIndex: 15)

		when:
		boolean result = service.markAsUndeployed(product, command, Stub(User) {
			isDevOps() >> true
		})

		then:
		result
	}

	@Unroll
	void "markAsUndeployed() transitions Product from #state to NOT_DEPLOYED"(Product.State state) {
		setup:
		setupProduct(state)
		service.permissionService = new PermissionService()

		def command = new ProductUndeployedCommand(blockNumber: 50000, blockIndex: 15)

		when:
		service.markAsUndeployed(product, command, Stub(User) {
			isDevOps() >> true
		})

		then:
		product.state == Product.State.NOT_DEPLOYED

		where:
		state << [Product.State.DEPLOYED, Product.State.UNDEPLOYING]
	}

	void "markAsUndeployed() invokes permissionService#systemRevokeAnonymousAccess"() {
		setup:
		setupProduct(Product.State.UNDEPLOYING)
		service.permissionService = Mock(PermissionService)

		def command = new ProductUndeployedCommand(blockNumber: 50000, blockIndex: 15)

		when:
		service.markAsUndeployed(product, command, Stub(User) {
			isDevOps() >> true
		})

		then:
		1 * service.permissionService.systemRevokeAnonymousAccess(product, Permission.Operation.PRODUCT_GET)
		0 * service.permissionService._
	}

	void "markAsDeployed() throws ValidationException if command object does not pass validation"() {
		setupProduct()
		when:
		service.markAsDeployed(product, new ProductDeployedCommand(), new User())
		then:
		thrown(ValidationException)
	}

	void "markAsDeployed() throws InvalidStateTransitionException if Product.state == UNDEPLOYING"() {
		setup:
		setupProduct(Product.State.UNDEPLOYING)

		def command = new ProductDeployedCommand(
			ownerAddress: "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
			beneficiaryAddress: "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
			pricePerSecond: "2",
			priceCurrency: Product.Currency.USD,
			minimumSubscriptionInSeconds: 600,
			blockNumber: 50000,
			blockIndex: 10
		)

		when:
		service.markAsDeployed(product, command, new User())
		then:
		thrown(InvalidStateTransitionException)
	}

	void "markAsDeployed() throws NotPermittedException if user is not devops"() {
		setup:
		setupProduct()

		def command = new ProductDeployedCommand(
			ownerAddress: "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
			beneficiaryAddress: "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
			pricePerSecond: "2",
			priceCurrency: Product.Currency.USD,
			minimumSubscriptionInSeconds: 600,
			blockNumber: 50000,
			blockIndex: 10
		)

		when:
		service.markAsDeployed(product, command, Stub(User) {
			isDevOps() >> false
		})
		then:
		thrown(NotPermittedException)
	}

	void "markAsDeployed() returns false if command object is stale"() {
		setup:
		setupProduct()
		service.permissionService = Stub(PermissionService)

		def command = new ProductDeployedCommand(
			ownerAddress: "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
			beneficiaryAddress: "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
			pricePerSecond: "2",
			priceCurrency: Product.Currency.USD,
			minimumSubscriptionInSeconds: 600,
			blockNumber: 30000,
			blockIndex: 10
		)

		when:
		boolean result = service.markAsDeployed(product, command, Stub(User) {
			isDevOps() >> true
		})

		then:
		!result
	}

	void "markAsDeployed() does not invoke permissionService#systemGrantAnonymousAccess if command object is stale"() {
		setup:
		setupProduct()
		service.permissionService = Mock(PermissionService)

		def command = new ProductDeployedCommand(
			ownerAddress: "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
			beneficiaryAddress: "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
			pricePerSecond: "2",
			priceCurrency: Product.Currency.USD,
			minimumSubscriptionInSeconds: 600,
			blockNumber: 30000,
			blockIndex: 10
		)

		when:
		boolean result = service.markAsDeployed(product, command, Stub(User) {
			isDevOps() >> true
		})

		then:
		0 * service.permissionService.systemGrantAnonymousAccess(_)
	}

	void "markAsDeployed() does not update Product if command object is stale"() {
		setup:
		setupProduct()
		service.permissionService = Stub(PermissionService)

		def command = new ProductDeployedCommand(
			ownerAddress: "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
			beneficiaryAddress: "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
			pricePerSecond: "2",
			priceCurrency: Product.Currency.USD,
			minimumSubscriptionInSeconds: 600,
			blockNumber: 30000,
			blockIndex: 10
		)

		when:
		service.markAsDeployed(product, command, Stub(User) {
			isDevOps() >> true
		})

		then:
		product.state == Product.State.NOT_DEPLOYED
		product.ownerAddress == "0x0000000000000000000000000000000000000000"
	}

	void "markAsDeployed() returns true if command not stale"() {
		setup:
		setupProduct()
		service.permissionService = Stub(PermissionService)

		def command = new ProductDeployedCommand(
			ownerAddress: "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
			beneficiaryAddress: "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
			pricePerSecond: "2",
			priceCurrency: Product.Currency.USD,
			minimumSubscriptionInSeconds: 600,
			blockNumber: 50000,
			blockIndex: 10
		)

		when:
		boolean result = service.markAsDeployed(product, command, Stub(User) {
			isDevOps() >> true
		})

		then:
		result
	}

	void "markAsDeployed() transitions Product to DEPLOYED and updates Blockchain-related information"() {
		setup:
		setupProduct()
		service.permissionService = Stub(PermissionService)

		def command = new ProductDeployedCommand(
			ownerAddress: "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
			beneficiaryAddress: "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
			pricePerSecond: "2",
			priceCurrency: Product.Currency.USD,
			minimumSubscriptionInSeconds: 600,
			blockNumber: 50000,
			blockIndex: 10
		)

		when:
		service.markAsDeployed(product, command, Stub(User) {
			isDevOps() >> true
		})

		then:
		Product.findById("product-id").toMap() == product.toMap()

		and:
		product.writtenToChain == true
		def map = product.toMap()
		map.id == "product-id"
		map.type == "NORMAL"
		map.name == "name"
		map.description == "description"
		map.imageUrl == null
		map.thumbnailUrl == null
		map.category == "category-id"
		map.streams == []
		map.previewStreamId == null
		map.previewConfigJson == null
		map.created == product.dateCreated
		map.updated == product.lastUpdated

		// changes below
		map.state == "DEPLOYED"
		map.ownerAddress == "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"
		map.beneficiaryAddress == "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
		map.pricePerSecond == "2"
		map.isFree == false
		map.priceCurrency == "USD"
		map.minimumSubscriptionInSeconds == 600
		map.owner == "Firstname Lastname"
	}

	void "markAsDeployed() grants public access to Product via permissionService#systemGrantAnonymousAccess"() {
		setup:
		setupProduct()
		service.permissionService = Mock(PermissionService)

		def command = new ProductDeployedCommand(
			ownerAddress: "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
			beneficiaryAddress: "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
			pricePerSecond: "2",
			priceCurrency: Product.Currency.USD,
			minimumSubscriptionInSeconds: 600,
			blockNumber: 50000,
			blockIndex: 10
		)

		when:
		def product = service.markAsDeployed(product, command, Stub(User) {
			isDevOps() >> true
		})

		then:
		1 * service.permissionService.systemGrantAnonymousAccess(product, Permission.Operation.PRODUCT_GET)
		0 * service.permissionService._
	}

	void "updatePricing() updates product price etc"() {
		setup:
		setupProduct(Product.State.DEPLOYED)
		service.permissionService = new PermissionService()

		def command = new SetPricingCommand(
			ownerAddress: "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
			beneficiaryAddress: "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
			pricePerSecond: "2",
			priceCurrency: Product.Currency.USD,
			minimumSubscriptionInSeconds: 600,
			blockNumber: 50000,
			blockIndex: 10
		)

		when:
		service.updatePricing(product, command, Stub(User) {
			isDevOps() >> true
		})

		then:
		product.ownerAddress == "0xBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"
		product.beneficiaryAddress == "0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
		product.pricePerSecond == "2"
		product.priceCurrency == Product.Currency.USD
		product.minimumSubscriptionInSeconds == 600
		product.blockNumber == 50000
		product.blockIndex == 10
	}

	void "updatePricing() throws NotPermittedException if user is not devops"() {
		setup:
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
		service.updatePricing(product, command, Stub(User) {
			isDevOps() >> false
		})
		then:
		thrown(NotPermittedException)
	}

	// Tests for Free products!

	void "deployFreeProduct throws ProductNotFreeException when given paid Product"() {
		when:
		service.deployFreeProduct(new Product(pricePerSecond: "2"))
		then:
		thrown(ProductNotFreeException)
	}

	void "deployFreeProduct throws InvalidStateTransitionException when given Product in state DEPLOYED"() {
		when:
		service.deployFreeProduct(new Product(pricePerSecond: "0", state: Product.State.DEPLOYED))
		then:
		thrown(InvalidStateTransitionException)
	}

	void "deployFreeProduct transitions Product state to DEPLOYED"() {
		setup:
		setupFreeProduct()
		service.permissionService = Stub(PermissionService)

		when:
		service.deployFreeProduct(freeProduct)
		then:
		Product.findById("free-product-id").state == Product.State.DEPLOYED
	}

	void "deployFreeProduct grants anonymous access to Product"() {
		setup:
		setupStreams()
		setupFreeProduct()
		service.permissionService = Mock(PermissionService)

		when:
		service.deployFreeProduct(freeProduct)
		then:
		1 * service.permissionService.systemGrantAnonymousAccess(freeProduct, Permission.Operation.PRODUCT_GET)
		0 * service.permissionService._
	}

	void "deployFreeProduct grants anonymous stream_get and stream_subscribe access to the streams of Product"() {
		setup:
		setupStreams()
		setupFreeProduct()
		service.permissionService = Mock(PermissionService)

		when:
		service.deployFreeProduct(freeProduct)
		then:
		1 * service.permissionService.systemGrantAnonymousAccess(freeProduct, Permission.Operation.PRODUCT_GET)
		0 * service.permissionService._
	}

	void "undeployFreeProduct throws ProductNotFreeException when given paid Product"() {
		setup:
		setupFreeProduct()
		when:
		service.undeployFreeProduct(new Product(pricePerSecond: "2"))
		then:
		thrown(ProductNotFreeException)
	}

	void "undeployFreeProduct throws InvalidStateTransitionException when given Product in state NOT_DEPLOYED"() {
		setup:
		setupFreeProduct()
		when:
		service.undeployFreeProduct(new Product(pricePerSecond: "0", state: Product.State.NOT_DEPLOYED))
		then:
		thrown(InvalidStateTransitionException)
	}

	void "undeployFreeProduct transitions Product state to NOT_DEPLOYED"() {
		setup:
		setupFreeProduct()
		service.permissionService = Stub(PermissionService)

		freeProduct.state = Product.State.DEPLOYED
		freeProduct.save(failOnError: true)

		when:
		service.undeployFreeProduct(freeProduct)
		then:
		Product.findById("free-product-id").state == Product.State.NOT_DEPLOYED
	}

	void "undeployFreeProduct revokes anonymous access to Product"() {
		setup:
		setupStreams()
		setupFreeProduct()
		service.permissionService = Mock(PermissionService)

		freeProduct.state = Product.State.DEPLOYED
		freeProduct.save(failOnError: true)

		when:
		service.undeployFreeProduct(freeProduct)
		then:
		1 * service.permissionService.systemRevokeAnonymousAccess(freeProduct, Permission.Operation.PRODUCT_GET)
		0 * service.permissionService._
	}

	void "undeployFreeProduct revokes anonymous stream_get and stream_subscribe access to the streams of Product"() {
		setup:
		setupStreams()
		setupFreeProduct()
		service.permissionService = Mock(PermissionService)

		freeProduct.state = Product.State.DEPLOYED
		freeProduct.save(failOnError: true)

		when:
		service.undeployFreeProduct(freeProduct)
		then:
		1 * service.permissionService.systemRevokeAnonymousAccess(freeProduct, Permission.Operation.PRODUCT_GET)
		0 * service.permissionService._
	}
}
