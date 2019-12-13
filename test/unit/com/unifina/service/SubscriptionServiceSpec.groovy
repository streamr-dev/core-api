package com.unifina.service

import com.unifina.BeanMockingSpecification
import com.unifina.api.ProductNotFreeException
import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.FreeSubscription
import com.unifina.domain.marketplace.PaidSubscription
import com.unifina.domain.marketplace.Product
import com.unifina.domain.marketplace.Subscription
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(SubscriptionService)
@Mock([FreeSubscription, IntegrationKey, PaidSubscription, Permission, Product, Stream, Subscription])
class SubscriptionServiceSpec extends BeanMockingSpecification {

	SecUser user, user2
	Stream s1, s2, s3
	Product product
	PermissionService permissionService
	EthereumIntegrationKeyService ethereumIntegrationKeyService

	void setup() {
		user = new SecUser(username: "me@streamr.com").save(failOnError: true, validate: false)
		user2 = new SecUser(username: "someoneElse@streamr.com").save(failOnError: true, validate: false)
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
		new IntegrationKey(
			user: user,
			name: "ik1",
			service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: "0x0000000000000000000000000000000000000005"
		).save(failOnError: true, validate: false)

		new IntegrationKey(
			user: user,
			name: "ik1",
			service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: "0x000000000000000000000000000000000000000C"
		).save(failOnError: true, validate: false)

		expect:
		service.getSubscriptionsOfUser(user) == []
	}

	void "getSubscriptionsOfUser() returns subscriptions tied to user's integration keys"() {
		new IntegrationKey(
			user: user,
			name: "ik1",
			service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: "0x0000000000000000000000000000000000000005"
		).save(failOnError: true, validate: false)

		new IntegrationKey(
			user: user,
			name: "ik1",
			service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: "0x000000000000000000000000000000000000000C"
		).save(failOnError: true, validate: false)

		def s1 = new PaidSubscription(address: "0x0000000000000000000000000000000000000000")
			.save(failOnError: true, validate: false)
		def s2 = new PaidSubscription(address: "0x0000000000000000000000000000000000000005")
			.save(failOnError: true, validate: false)
		def s3 = new PaidSubscription(address: "0x000000000000000000000000000000000000000C")
			.save(failOnError: true, validate: false)
		def s4 = new PaidSubscription(address: "0x000000000000000000000000000000000000000C")
			.save(failOnError: true, validate: false)


		expect:
		service.getSubscriptionsOfUser(user) == [s2, s3, s4]
	}

	void "getSubscriptionsOfUser() returns free subscriptions as well"() {
		def product2 = new Product().save(failOnError: true, validate: false)
		def s1 = new FreeSubscription(product: product, user: user, endsAt: new Date(2018, 4, 13, 13, 6))
			.save(failOnError: true)
		def s2 = new FreeSubscription(product: product2, user: user, endsAt: new Date(2018, 10, 20, 14, 0))
			.save(failOnError: true)
		def s3 = new FreeSubscription(product: product2, user: user2, endsAt: new Date(2018, 10, 20, 14, 0))
			.save(failOnError: true)
		def s4 = new FreeSubscription(product: product, user: user2, endsAt: new Date(2018, 10, 20, 14, 0))
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
		PaidSubscription.findAll() == [s]
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
		new IntegrationKey(
			user: user,
			idInService: address,
			service: IntegrationKey.Service.ETHEREUM_ID
		).save(failOnError: true, validate: false)
		Permission p1 = new Permission(operation: "READ", user: user, stream: s1).save(failOnError: true, validate: false)
		Permission p2 = new Permission(operation: "READ", user: user, stream: s2).save(failOnError: true, validate: false)

		when:

		service.onSubscribed(product, address, new Date())

		then:
		1 * ethereumIntegrationKeyService.getEthereumUser(address) >> user
		1 * permissionService.systemGrant(user, s1, Permission.Operation.READ, _, _) >> p1
		1 * permissionService.systemGrant(user, s2, Permission.Operation.READ, _, _) >> p2
		Permission.findAll()*.toInternalMap() as Set == [p1.toInternalMap(), p2.toInternalMap()] as Set
	}

	void "onSubscribed() does not remove existing non-subscription permissions if user found for address"() {
		String address = "0x0000000000000000000000000000000000000000"

		Permission p1 = new Permission(user: user, stream: s1, operation: "READ").save(failOnError: true, validate: false)
		Permission p2 = new Permission(user: user, stream: s2, operation: "READ").save(failOnError: true, validate: false)

		assert Permission.exists(p1.id)
		assert Permission.exists(p2.id)

		new IntegrationKey(
			user: user,
			idInService: address,
			service: IntegrationKey.Service.ETHEREUM_ID
		).save(failOnError: true, validate: false)

		Permission p3 = new Permission(operation: "READ", user: user, stream: s1, endsAt: new Date())
		Permission p4 = new Permission(operation: "READ", user: user, stream: s2, endsAt: new Date())

		when:
		service.onSubscribed(product, address, new Date())

		then:
		1 * ethereumIntegrationKeyService.getEthereumUser(address) >> user
		1 * permissionService.systemGrant(user, s1, Permission.Operation.READ, _, _) >> p3
		1 * permissionService.systemGrant(user, s2, Permission.Operation.READ, _, _) >> p4
		Permission.exists(p1.id)
		Permission.exists(p2.id)
	}

	void "onSubscribed() removes existing subscription-linked permissions if user found for address"() {
		String address = "0x0000000000000000000000000000000000000000"
		Subscription s = new PaidSubscription(product: product, address: address, endsAt: new Date(0))
			.save(failOnError: true, validate: false)

		Permission p1 = new Permission(user: user, stream: s1, operation: "READ")
		Permission p2 = new Permission(user: user, stream: s2, operation: "READ")
		p1.subscription = s
		p2.subscription = s
		p1.save(failOnError: true, validate: false)
		p2.save(failOnError: true, validate: false)

		assert Permission.exists(p1.id)
		assert Permission.exists(p2.id)

		new IntegrationKey(
			user: user,
			idInService: address,
			service: IntegrationKey.Service.ETHEREUM_ID
		).save(failOnError: true, validate: false)

		Permission p3 = new Permission(operation: "READ", user: user, stream: s1, endsAt: new Date())
		Permission p4 = new Permission(operation: "READ", user: user, stream: s2, endsAt: new Date())

		when:
		service.onSubscribed(product, address, new Date())

		then:
		1 * ethereumIntegrationKeyService.getEthereumUser(address) >> user
		1 * permissionService.systemGrant(user, s1, Permission.Operation.READ, _, _) >> p3
		1 * permissionService.systemGrant(user, s2, Permission.Operation.READ, _, _) >> p4
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

		assert FreeSubscription.count() == 0
		Permission p1 = new Permission(operation: "READ", user: user, stream: s1)
		Permission p2 = new Permission(operation: "READ", user: user, stream: s2)

		when:
		def s = service.subscribeToFreeProduct(product, user, new Date())

		then:
		1 * permissionService.systemGrant(user, s1, Permission.Operation.READ, _, _) >> p1
		1 * permissionService.systemGrant(user, s2, Permission.Operation.READ, _, _) >> p2
		s.id != null
		FreeSubscription.findAll() == [s]
	}

	void "subscribeToFreeProduct() updates existing Subscription if product-user exists"() {
		product.pricePerSecond = 0

		Subscription sub1 = new FreeSubscription(product: product, user: user, endsAt: new Date()).save(failOnError: true, validate: false)
		Permission p1 = new Permission(operation: "READ", user: user, stream: s1)
		Permission p2 = new Permission(operation: "READ", user: user, stream: s2)

		when:
		def newDate = new Date()
		def sub2 = service.subscribeToFreeProduct(product, user, newDate)

		then:
		1 * permissionService.systemGrant(user, s1, Permission.Operation.READ, _, _) >> p1
		1 * permissionService.systemGrant(user, s2, Permission.Operation.READ, _, _) >> p2
		sub2.id == sub1.id
		Subscription.count() == 1
		Subscription.findById(sub2.id).endsAt == newDate
	}

	void "subscribeToFreeProduct() creates subscription-linked permissions"() {
		product.pricePerSecond = 0

		assert Permission.count() == 0

		Permission p1 = new Permission(operation: "READ", user: user, stream: s1).save(failOnError: true, validate: false)
		Permission p2 = new Permission(operation: "READ", user: user, stream: s2).save(failOnError: true, validate: false)

		when:
		service.subscribeToFreeProduct(product, user, new Date())

		then:
		1 * permissionService.systemGrant(user, s1, Permission.Operation.READ, _, _) >> p1
		1 * permissionService.systemGrant(user, s2, Permission.Operation.READ, _, _) >> p2
		Permission.findAll()*.toInternalMap() as Set == [p1.toInternalMap(), p2.toInternalMap()] as Set
	}

	void "subscribeToFreeProduct() removes existing subscription-linked permissions"() {
		product.pricePerSecond = 0

		Subscription s = new FreeSubscription(product: product, user: user2, endsAt: new Date(0))
			.save(failOnError: true, validate: false)
		Permission p1 = new Permission(user: user, stream: s1, operation: Permission.Operation.READ)
		Permission p2 = new Permission(user: user, stream: s2, operation: Permission.Operation.READ)
		p1.subscription = s
		p2.subscription = s
		p1.save(failOnError: true, validate: false)
		p2.save(failOnError: true, validate: false)

		assert Permission.exists(p1.id)
		assert Permission.exists(p2.id)

		Permission p3 = new Permission(operation: "READ", user: user, stream: s1)
		Permission p4 = new Permission(operation: "READ", user: user, stream: s2)

		when:
		service.subscribeToFreeProduct(product, user2, new Date())

		then:
		1 * permissionService.systemGrant(user2, s1, Permission.Operation.READ, _, _) >> p3
		1 * permissionService.systemGrant(user2, s2, Permission.Operation.READ, _, _) >> p4
		1 * permissionService.systemRevoke(p1) >> { p1.delete(flush: true) }
		1 * permissionService.systemRevoke(p2) >> { p2.delete(flush: true) }
		!Permission.exists(p1.id)
		!Permission.exists(p2.id)
	}

	void "beforeIntegrationKeyRemoved() removes all subscription-linked permissions for given integration key"() {
		String address1 = "0x0000000000000000000000000000000000000000"
		String address2 = "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"

		def integrationKey = new IntegrationKey(
			user: user,
			idInService: address1,
			service: IntegrationKey.Service.ETHEREUM_ID
		).save(failOnError: true, validate: false)

		new IntegrationKey(
			user: user2,
			idInService: address2,
			service: IntegrationKey.Service.ETHEREUM_ID
		).save(failOnError: true, validate: false)

		def product2 = new Product(streams: [s3]).save(failOnError: true, validate: false)

		setup: "create permissions"
		Subscription sub1 = new PaidSubscription(product: product, address: address1).save(failOnError: true, validate: false)
		Permission p1 = new Permission(stream: s1, subscription: sub1, endsAt: new Date()).save(failOnError: true, validate: false)
		Permission p2 = new Permission(stream: s2, subscription: sub1, endsAt: new Date()).save(failOnError: true, validate: false)

		Subscription sub2 = new PaidSubscription(product: product2, address: address1).save(failOnError: true, validate: false)
		Permission p3 = new Permission(stream: s3, subscription: sub2, endsAt: new Date()).save(failOnError: true, validate: false)

		Subscription sub3 = new PaidSubscription(product: product, address: address2).save(failOnError: true, validate: false)
		new Permission(stream: s1, subscription: sub3, endsAt: new Date()).save(failOnError: true, validate: false)
		new Permission(stream: s2, subscription: sub3, endsAt: new Date()).save(failOnError: true, validate: false)

		Subscription sub4 = new PaidSubscription(product: product2, address: address2).save(failOnError: true, validate: false)
		new Permission(stream: s3, subscription: sub4, endsAt: new Date()).save(failOnError: true, validate: false)

		new Permission(user: user, stream: s1, operation: "READ").save(failOnError: true, validate: false)
		assert Permission.count() == 2 + 1 + 2 + 1 + 1

		when:
		service.beforeIntegrationKeyRemoved(integrationKey)
		then:
		1 * permissionService.systemRevoke(p1) >> { p1.delete(flush: true) }
		1 * permissionService.systemRevoke(p2) >> { p2.delete(flush: true) }
		1 * permissionService.systemRevoke(p3) >> { p3.delete(flush: true) }
		Permission.findAll()*.id == [4L, 5L, 6L, 7L]
	}

	void "afterIntegrationKeyCreated() creates subscription-linked permissions for given integration key"() {
		def date = new Date()
		def product2 = new Product(streams: [s3]).save(failOnError: true, validate: false)
		String address = "0x0000000000000000000000000000000000000000"
		new PaidSubscription(
			address: address,
			product: product,
			endsAt: date
		).save(failOnError: true, validate: true)
		new PaidSubscription(
			address: address,
			product: product2,
			endsAt: date
		).save(failOnError: true, validate: true)

		def integrationKey = new IntegrationKey(
			user: user,
			idInService: address,
			service: IntegrationKey.Service.ETHEREUM_ID
		).save(failOnError: true, validate: false)

		Permission p1 = new Permission(operation: "READ", user: user, stream: s1).save(failOnError: true, validate: false)
		Permission p2 = new Permission(operation: "READ", user: user, stream: s2).save(failOnError: true, validate: false)
		Permission p3 = new Permission(operation: "READ", user: user, stream: s3).save(failOnError: true, validate: false)

		when:
		service.afterIntegrationKeyCreated(integrationKey)

		then:
		2 * ethereumIntegrationKeyService.getEthereumUser(address) >> user
		1 * permissionService.systemGrant(user, s1, Permission.Operation.READ, _, _) >> p1
		1 * permissionService.systemGrant(user, s2, Permission.Operation.READ, _, _) >> p2
		1 * permissionService.systemGrant(user, s3, Permission.Operation.READ, _, _) >> p3
		Permission.findAll()*.toInternalMap() as Set == [p1.toInternalMap(), p2.toInternalMap(), p3.toInternalMap()] as Set
	}

	void "afterIntegrationKeyCreated() updates subscription-linked permissions on a per-user basis"() {
		String address1 = "0x0000000000000000000000000000000000000000"
		String address2 = "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"

		new IntegrationKey(
			user: user,
			idInService: address1,
			service: IntegrationKey.Service.ETHEREUM_ID
		).save(failOnError: true, validate: false)

		new IntegrationKey(
			user: user2,
			idInService: address2,
			service: IntegrationKey.Service.ETHEREUM_ID
		).save(failOnError: true, validate: false)

		Subscription sub1, sub2
		Permission p1, p2, p3, p4

		setup: "create permissions"
		def date = new Date()
		sub1 = new PaidSubscription(product: product, address: address1).save(failOnError: true, validate: false)
		p1 = new Permission(user: user, stream: s1, operation: "READ", subscription: sub1, endsAt: date).save(failOnError: true, validate: false)
		p2 = new Permission(user: user, stream: s2, operation: "READ", subscription: sub1, endsAt: date).save(failOnError: true, validate: false)

		product.streams = [s1]
		product.save(failOnError: true, validate: false)

		sub2 = new PaidSubscription(product: product, address: address2).save(failOnError: true, validate: false)
		p3 = new Permission(user: user2, stream: s3, operation: "READ", subscription: sub2, endsAt: date).save(failOnError: true, validate: false)

		assert Permission.findAll()*.toInternalMap() as Set == [p1.toInternalMap(), p2.toInternalMap(), p3.toInternalMap()] as Set

		and: "change product"
		product.streams = [s2, s3]
		product.save(failOnError: true, validate: false)

		when:
		service.afterProductUpdated(product)
		then:
		interaction {
			p1 = new Permission(operation: "READ", user: user, stream: s3).save(failOnError: true, validate: false)
			p4 = new Permission(operation: "READ", user: user2, stream: s2).save(failOnError: true, validate: false)
			1 * ethereumIntegrationKeyService.getEthereumUser(address1) >> user
			1 * ethereumIntegrationKeyService.getEthereumUser(address2) >> user2
			1 * permissionService.systemGrant(user, s3, Permission.Operation.READ, _, _) >> p1
			1 * permissionService.systemGrant(user2, s2, Permission.Operation.READ, _, _) >> p4
		}
		Permission.findAll()*.toInternalMap() as Set == [p1.toInternalMap(), p2.toInternalMap(),
														 p3.toInternalMap(), p4.toInternalMap()] as Set
	}
}
