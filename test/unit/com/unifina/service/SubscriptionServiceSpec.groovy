package com.unifina.service

import com.unifina.BeanMockingSpecification
import com.unifina.domain.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(SubscriptionService)
@Mock([SubscriptionFree, SubscriptionPaid, Permission, Product, Stream, Subscription])
class SubscriptionServiceSpec extends BeanMockingSpecification {

	User user, user2
	Stream s1, s2, s3
	Product product
	PermissionService permissionService
	EthereumIntegrationKeyService ethereumIntegrationKeyService

	void setup() {
		user = new User(username: "0x0000000000000000000000000000000000000005").save(failOnError: true, validate: false)
		user2 = new User(username: "0x000000000000000000000000000000000000000C").save(failOnError: true, validate: false)
		s1 = new Stream(name: "stream-1")
		s2 = new Stream(name: "stream-2")
		s3 = new Stream(name: "stream-3")

		[s1, s2, s3].eachWithIndex { s, i -> s.id = "stream-${i + 1}" }
		[s1, s2, s3]*.save(failOnError: true, validate: false)
		product = new Product(streams: [s1, s2]).save(failOnError: true, validate: false)
		permissionService = service.permissionService = mockBean(PermissionService, Mock(PermissionService))
		ethereumIntegrationKeyService = mockBean(EthereumIntegrationKeyService, Mock(EthereumIntegrationKeyService))
	}

	void "getSubscriptionsOfUser() returns empty if user has no integration keys or free subscriptions"() {
		expect:
		service.getSubscriptionsOfUser(user) == []
	}

	void "getSubscriptionsOfUser() returns empty if user has no subscriptions tied to integration keys and no free subscriptions"() {
		expect:
		service.getSubscriptionsOfUser(user) == []
	}

	void "getSubscriptionsOfUser() returns free subscriptions as well"() {
		def product2 = new Product().save(failOnError: true, validate: false)
		def s1 = new SubscriptionFree(product: product, user: user, endsAt: new Date(2018, 4, 13, 13, 6))
			.save(failOnError: true)
		def s2 = new SubscriptionFree(product: product2, user: user, endsAt: new Date(2018, 10, 20, 14, 0))
			.save(failOnError: true)
		def s3 = new SubscriptionFree(product: product2, user: user2, endsAt: new Date(2018, 10, 20, 14, 0))
			.save(failOnError: true)
		def s4 = new SubscriptionFree(product: product, user: user2, endsAt: new Date(2018, 10, 20, 14, 0))
			.save(failOnError: true)

		expect:
		service.getSubscriptionsOfUser(user) == [s1, s2]
	}

	void "onSubscribed() creates new PaidSubscription if product-address pair does not exist"() {
		assert Subscription.count() == 0

		when:
		def s = service.onSubscribed(product, "0x0000000000000000000000000000000000000000", new Date())

		then:
		s.id != null
		SubscriptionPaid.findAll() == [s]
	}

	void "onSubscribed() updates existing Subscription if product-address exists"() {
		def s1 = service.onSubscribed(product, "0x0000000000000000000000000000000000000000", new Date(0))

		when:
		def newDate = new Date()
		def s2 = service.onSubscribed(product, "0x0000000000000000000000000000000000000000", newDate)

		then:
		s2.id == s1.id
		Subscription.count() == 1
		Subscription.findById(s2.id).endsAt == newDate
	}

	void "onSubscribed() does not create any permissions if user not found for address"() {
		when:
		service.onSubscribed(product, "0x0000000000000000000000000000000000000000", new Date())

		then:
		Permission.count() == 0
	}

	void "onSubscribed() creates subscription-linked permissions if user found for address"() {
		String address = "0x0000000000000000000000000000000000000000"
		Permission p1 = new Permission(operation: Permission.Operation.STREAM_SUBSCRIBE, user: user, stream: s1).save(failOnError: true, validate: false)
		Permission p2 = new Permission(operation: Permission.Operation.STREAM_SUBSCRIBE, user: user, stream: s2).save(failOnError: true, validate: false)

		when:

		service.onSubscribed(product, address, new Date())

		then:
		1 * ethereumIntegrationKeyService.getEthereumUser(address) >> user
		1 * permissionService.systemGrant(user, s1, Permission.Operation.STREAM_SUBSCRIBE, _, _) >> p1
		1 * permissionService.systemGrant(user, s2, Permission.Operation.STREAM_SUBSCRIBE, _, _) >> p2
		Permission.findAll()*.toInternalMap() as Set == [p1.toInternalMap(), p2.toInternalMap()] as Set
	}

	void "onSubscribed() does not remove existing non-subscription permissions if user found for address"() {
		String address = "0x0000000000000000000000000000000000000000"

		Permission p1 = new Permission(user: user, stream: s1, operation: Permission.Operation.STREAM_SUBSCRIBE).save(failOnError: true, validate: false)
		Permission p2 = new Permission(user: user, stream: s2, operation: Permission.Operation.STREAM_SUBSCRIBE).save(failOnError: true, validate: false)

		assert Permission.exists(p1.id)
		assert Permission.exists(p2.id)

		Permission p3 = new Permission(operation: Permission.Operation.STREAM_SUBSCRIBE, user: user, stream: s1, endsAt: new Date())
		Permission p4 = new Permission(operation: Permission.Operation.STREAM_SUBSCRIBE, user: user, stream: s2, endsAt: new Date())

		when:
		service.onSubscribed(product, address, new Date())

		then:
		1 * ethereumIntegrationKeyService.getEthereumUser(address) >> user
		1 * permissionService.systemGrant(user, s1, Permission.Operation.STREAM_SUBSCRIBE, _, _) >> p3
		1 * permissionService.systemGrant(user, s2, Permission.Operation.STREAM_SUBSCRIBE, _, _) >> p4
		Permission.exists(p1.id)
		Permission.exists(p2.id)
	}

	void "onSubscribed() removes existing subscription-linked permissions if user found for address"() {
		String address = "0x0000000000000000000000000000000000000000"
		Subscription s = new SubscriptionPaid(product: product, address: address, endsAt: new Date(0))
			.save(failOnError: true, validate: false)

		Permission p1 = new Permission(user: user, stream: s1, operation: Permission.Operation.STREAM_SUBSCRIBE)
		Permission p2 = new Permission(user: user, stream: s2, operation: Permission.Operation.STREAM_SUBSCRIBE)
		p1.subscription = s
		p2.subscription = s
		p1.save(failOnError: true, validate: false)
		p2.save(failOnError: true, validate: false)

		assert Permission.exists(p1.id)
		assert Permission.exists(p2.id)

		Permission p3 = new Permission(operation: Permission.Operation.STREAM_SUBSCRIBE, user: user, stream: s1, endsAt: new Date())
		Permission p4 = new Permission(operation: Permission.Operation.STREAM_SUBSCRIBE, user: user, stream: s2, endsAt: new Date())

		when:
		service.onSubscribed(product, address, new Date())

		then:
		1 * ethereumIntegrationKeyService.getEthereumUser(address) >> user
		1 * permissionService.systemGrant(user, s1, Permission.Operation.STREAM_SUBSCRIBE, _, _) >> p3
		1 * permissionService.systemGrant(user, s2, Permission.Operation.STREAM_SUBSCRIBE, _, _) >> p4
		1 * permissionService.systemRevoke(p1) >> { p1.delete(flush: true) }
		1 * permissionService.systemRevoke(p2) >> { p2.delete(flush: true) }
		!Permission.exists(p1.id)
		!Permission.exists(p2.id)
	}

	void "subscribeToFreeProduct() throws ProductNotFreeException if given non-free Product"() {
		product.pricePerSecond = 1
		product.priceCurrency = Product.Currency.DATA

		when:
		service.subscribeToFreeProduct(product, user, new Date())
		then:
		thrown(ProductNotFreeException)
	}

	void "subscribeToFreeProduct() creates new FreeSubscription if product-user pair does not exist"() {
		product.pricePerSecond = 0

		assert SubscriptionFree.count() == 0
		Permission p1 = new Permission(operation: Permission.Operation.STREAM_SUBSCRIBE, user: user, stream: s1)
		Permission p2 = new Permission(operation: Permission.Operation.STREAM_SUBSCRIBE, user: user, stream: s2)

		when:
		def s = service.subscribeToFreeProduct(product, user, new Date())

		then:
		1 * permissionService.systemGrant(user, s1, Permission.Operation.STREAM_SUBSCRIBE, _, _) >> p1
		1 * permissionService.systemGrant(user, s2, Permission.Operation.STREAM_SUBSCRIBE, _, _) >> p2
		s.id != null
		SubscriptionFree.findAll() == [s]
	}

	void "subscribeToFreeProduct() updates existing Subscription if product-user exists"() {
		product.pricePerSecond = 0

		Subscription sub1 = new SubscriptionFree(product: product, user: user, endsAt: new Date()).save(failOnError: true, validate: false)
		Permission p1 = new Permission(operation: Permission.Operation.STREAM_SUBSCRIBE, user: user, stream: s1)
		Permission p2 = new Permission(operation: Permission.Operation.STREAM_SUBSCRIBE, user: user, stream: s2)

		when:
		def newDate = new Date()
		def sub2 = service.subscribeToFreeProduct(product, user, newDate)

		then:
		1 * permissionService.systemGrant(user, s1, Permission.Operation.STREAM_SUBSCRIBE, _, _) >> p1
		1 * permissionService.systemGrant(user, s2, Permission.Operation.STREAM_SUBSCRIBE, _, _) >> p2
		sub2.id == sub1.id
		Subscription.count() == 1
		Subscription.findById(sub2.id).endsAt == newDate
	}

	void "subscribeToFreeProduct() creates subscription-linked permissions"() {
		product.pricePerSecond = 0

		assert Permission.count() == 0

		Permission p1 = new Permission(operation: Permission.Operation.STREAM_SUBSCRIBE, user: user, stream: s1).save(failOnError: true, validate: false)
		Permission p2 = new Permission(operation: Permission.Operation.STREAM_SUBSCRIBE, user: user, stream: s2).save(failOnError: true, validate: false)

		when:
		service.subscribeToFreeProduct(product, user, new Date())

		then:
		1 * permissionService.systemGrant(user, s1, Permission.Operation.STREAM_SUBSCRIBE, _, _) >> p1
		1 * permissionService.systemGrant(user, s2, Permission.Operation.STREAM_SUBSCRIBE, _, _) >> p2
		Permission.findAll()*.toInternalMap() as Set == [p1.toInternalMap(), p2.toInternalMap()] as Set
	}

	void "subscribeToFreeProduct() removes existing subscription-linked permissions"() {
		product.pricePerSecond = 0

		Subscription s = new SubscriptionFree(product: product, user: user2, endsAt: new Date(0))
			.save(failOnError: true, validate: false)
		Permission p1 = new Permission(user: user, stream: s1, operation: Permission.Operation.STREAM_SUBSCRIBE)
		Permission p2 = new Permission(user: user, stream: s2, operation: Permission.Operation.STREAM_SUBSCRIBE)
		p1.subscription = s
		p2.subscription = s
		p1.save(failOnError: true, validate: false)
		p2.save(failOnError: true, validate: false)

		assert Permission.exists(p1.id)
		assert Permission.exists(p2.id)

		Permission p3 = new Permission(operation: Permission.Operation.STREAM_SUBSCRIBE, user: user, stream: s1)
		Permission p4 = new Permission(operation: Permission.Operation.STREAM_SUBSCRIBE, user: user, stream: s2)

		when:
		service.subscribeToFreeProduct(product, user2, new Date())

		then:
		1 * permissionService.systemGrant(user2, s1, Permission.Operation.STREAM_SUBSCRIBE, _, _) >> p3
		1 * permissionService.systemGrant(user2, s2, Permission.Operation.STREAM_SUBSCRIBE, _, _) >> p4
		1 * permissionService.systemRevoke(p1) >> { p1.delete(flush: true) }
		1 * permissionService.systemRevoke(p2) >> { p2.delete(flush: true) }
		!Permission.exists(p1.id)
		!Permission.exists(p2.id)
	}
}
