package com.unifina.service

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
import spock.lang.Specification

@TestFor(SubscriptionService)
@Mock([FreeSubscription, IntegrationKey, PaidSubscription, Permission, Product, Stream, Subscription])
class SubscriptionServiceSpec extends Specification {

	SecUser user, user2
	Stream s1, s2, s3
	Product product

	void setup() {
		user = new SecUser(username: "me@streamr.com").save(failOnError: true, validate: false)
		user2 = new SecUser(username: "someoneElse@streamr.com").save(failOnError: true, validate: false)
		s1 = new Stream(name: "stream-1")
		s2 = new Stream(name: "stream-2")
		s3 = new Stream(name: "stream-3")
		[s1, s2, s3].eachWithIndex { s, i -> s.id = "stream-${i + 1}" }
		[s1, s2, s3]*.save(failOnError: true, validate: false)
		product = new Product(streams: [s1, s2]).save(failOnError: true, validate: false)
		service.permissionService = new PermissionService()
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
		new IntegrationKey(
			user: user,
			idInService: "0x0000000000000000000000000000000000000000",
			service: IntegrationKey.Service.ETHEREUM_ID
		).save(failOnError: true, validate: false)

		when:
		def date = new Date()
		service.onSubscribed(product, "0x0000000000000000000000000000000000000000", date)

		then:
		Permission.findAll()*.toInternalMap() as Set == [
			[
				operation: "READ",
				user: 1L,
				stream: "stream-1",
				subscription: 1L,
				endsAt: date
			],
			[
				operation: "READ",
				user: 1L,
				stream: "stream-2",
				subscription: 1L,
				endsAt: date
			]
		] as Set
	}

	void "onSubscribed() does not remove existing non-subscription permissions if user found for address"() {
		def permissionService = service.permissionService

		Permission p1 = permissionService.systemGrant(user, s1, Permission.Operation.READ)
		Permission p2 = permissionService.systemGrant(user, s2, Permission.Operation.READ)

		assert Permission.exists(p1.id)
		assert Permission.exists(p2.id)

		new IntegrationKey(
			user: user,
			idInService: "0x0000000000000000000000000000000000000000",
			service: IntegrationKey.Service.ETHEREUM_ID
		).save(failOnError: true, validate: false)

		when:
		service.onSubscribed(product, "0x0000000000000000000000000000000000000000", new Date())

		then:
		Permission.exists(p1.id)
		Permission.exists(p2.id)
	}

	void "onSubscribed() removes existing subscription-linked permissions if user found for address"() {
		def permissionService = service.permissionService

		def s = service.onSubscribed(product, "0x0000000000000000000000000000000000000000", new Date(0))

		Permission p1 = permissionService.systemGrant(user, s1, Permission.Operation.READ)
		Permission p2 = permissionService.systemGrant(user, s2, Permission.Operation.READ)
		p1.subscription = s
		p2.subscription = s
		p1.save(failOnError: true)
		p2.save(failOnError: true)

		assert Permission.exists(p1.id)
		assert Permission.exists(p2.id)

		new IntegrationKey(
			user: user,
			idInService: "0x0000000000000000000000000000000000000000",
			service: IntegrationKey.Service.ETHEREUM_ID
		).save(failOnError: true, validate: false)

		when:
		service.onSubscribed(product, "0x0000000000000000000000000000000000000000", new Date())

		then:
		!Permission.exists(p1.id)
		!Permission.exists(p2.id)
	}

	void "subscribeToFreeProduct() throws ProductNotFreeException if given non-free Product"() {
		when:
		service.subscribeToFreeProduct(product, user, new Date())
		then:
		thrown(ProductNotFreeException)
	}

	void "subscribeToFreeProduct() creates new FreeSubscription if product-user pair does not exist"() {
		product.pricePerSecond = 0

		assert FreeSubscription.count() == 0

		when:
		def s = service.subscribeToFreeProduct(product, user, new Date())

		then:
		s.id != null
		FreeSubscription.findAll() == [s]
	}

	void "subscribeToFreeProduct() updates existing Subscription if product-user exists"() {
		product.pricePerSecond = 0

		def s1 = service.subscribeToFreeProduct(product, user, new Date(0))

		when:
		def newDate = new Date()
		def s2 = service.subscribeToFreeProduct(product, user, newDate)

		then:
		s2.id == s1.id
		Subscription.count() == 1
		Subscription.findById(s2.id).endsAt == newDate
	}

	void "subscribeToFreeProduct() creates subscription-linked permissions"() {
		product.pricePerSecond = 0

		assert Permission.count() == 0

		when:
		def endDate = new Date()
		service.subscribeToFreeProduct(product, user, endDate)

		then:
		Permission.findAll()*.toInternalMap() as Set == [
			[
				operation: "READ",
				user: 1L,
				stream: "stream-1",
				subscription: 1L,
				endsAt: endDate
			],
			[
				operation: "READ",
				user: 1L,
				stream: "stream-2",
				subscription: 1L,
				endsAt: endDate
			]
		] as Set
	}

	void "subscribeToFreeProduct() removes existing subscription-linked permissions"() {
		product.pricePerSecond = 0

		def s = service.subscribeToFreeProduct(product, user2, new Date(0))
		Permission p1 = service.permissionService.systemGrant(user, s1, Permission.Operation.READ)
		Permission p2 = service.permissionService.systemGrant(user, s2, Permission.Operation.READ)
		p1.subscription = s
		p2.subscription = s
		p1.save(failOnError: true)
		p2.save(failOnError: true)

		assert Permission.exists(p1.id)
		assert Permission.exists(p2.id)

		when:
		service.subscribeToFreeProduct(product, user2, new Date())

		then:
		!Permission.exists(p1.id)
		!Permission.exists(p2.id)
	}

	void "beforeIntegrationKeyRemoved() throws IllegalArgumentException given IntegrationKey with service != ETHEREUM_ID"() {
		when:
		service.beforeIntegrationKeyRemoved(new IntegrationKey())
		then:
		def e = thrown(IllegalArgumentException)
		e.message.contains("ETHEREUM_ID")
	}

	void "beforeIntegrationKeyRemoved() removes all subscription-linked permissions for given integration key"() {
		def permissionService = service.permissionService = new PermissionService()

		def integrationKey = new IntegrationKey(
			user: user,
			idInService: "0x0000000000000000000000000000000000000000",
			service: IntegrationKey.Service.ETHEREUM_ID
		).save(failOnError: true, validate: false)

		new IntegrationKey(
			user: user2,
			idInService: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
			service: IntegrationKey.Service.ETHEREUM_ID
		).save(failOnError: true, validate: false)

		def product2 = new Product(streams: [s3]).save(failOnError: true, validate: false)

		setup: "create permissions"
		service.onSubscribed(product, "0x0000000000000000000000000000000000000000", new Date())
		service.onSubscribed(product2, "0x0000000000000000000000000000000000000000", new Date())
		service.onSubscribed(product, "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", new Date())
		service.onSubscribed(product2, "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", new Date())
		permissionService.systemGrant(user, s1, Permission.Operation.READ)
		assert Permission.count() == 2 + 1 + 2 + 1 + 1

		when:
		service.beforeIntegrationKeyRemoved(integrationKey)
		then:
		Permission.findAll()*.id == [4L, 5L, 6L, 7L]
	}

	void "afterIntegrationKeyCreated() throws IllegalArgumentException given IntegrationKey with service != ETHEREUM_ID"() {
		when:
		service.afterIntegrationKeyCreated(new IntegrationKey())
		then:
		def e = thrown(IllegalArgumentException)
		e.message.contains("ETHEREUM_ID")
	}

	void "afterIntegrationKeyCreated() creates subscription-linked permissions for given integration key"() {
		service.permissionService = new PermissionService()

		def date = new Date()
		def product2 = new Product(streams: [s3]).save(failOnError: true, validate: false)
		def sub1 = new PaidSubscription(
			address: "0x0000000000000000000000000000000000000000",
			product: product,
			endsAt: date
		).save(failOnError: true, validate: true)
		def sub2 = new PaidSubscription(
			address: "0x0000000000000000000000000000000000000000",
			product: product2,
			endsAt: date
		).save(failOnError: true, validate: true)

		def integrationKey = new IntegrationKey(
			user: user,
			idInService: "0x0000000000000000000000000000000000000000",
			service: IntegrationKey.Service.ETHEREUM_ID
		).save(failOnError: true, validate: false)

		when:
		service.afterIntegrationKeyCreated(integrationKey)

		then:
		Permission.findAll()*.toInternalMap() as Set == [
		    [
		        operation: "READ",
				user: 1L,
				stream: "stream-1",
				subscription: 1L,
				endsAt: date
		    ],
			[
				operation: "READ",
				user: 1L,
				stream: "stream-2",
				subscription: 1L,
				endsAt: date
			],
			[
				operation: "READ",
				user: 1L,
				stream: "stream-3",
				subscription: 2L,
				endsAt: date
			]
		] as Set
	}

	void "afterIntegrationKeyCreated() updates subscription-linked permissions on a per-user basis"() {
		service.permissionService = new PermissionService()

		new IntegrationKey(
			user: user,
			idInService: "0x0000000000000000000000000000000000000000",
			service: IntegrationKey.Service.ETHEREUM_ID
		).save(failOnError: true, validate: false)

		new IntegrationKey(
			user: user2,
			idInService: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
			service: IntegrationKey.Service.ETHEREUM_ID
		).save(failOnError: true, validate: false)

		setup: "create permissions"
		def date = new Date()
		service.onSubscribed(product, "0x0000000000000000000000000000000000000000", date) // 1, 2

		product.streams = [s1]
		product.save(failOnError: true, validate: false)
		service.onSubscribed(product, "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", date) // 3

		assert Permission.findAll()*.toInternalMap() as Set == [
			[operation: "READ", subscription: 1L, user: 1L, stream: "stream-1", endsAt: date],
			[operation: "READ", subscription: 1L, user: 1L, stream: "stream-2", endsAt: date],
			[operation: "READ", subscription: 2L, user: 2L, stream: "stream-1", endsAt: date],
		] as Set

		and: "change product"
		product.streams = [s2, s3]
		product.save(failOnError: true, validate: false)

		when:
		service.afterProductUpdated(product)
		then:
		Permission.findAll()*.toInternalMap() as Set == [
			[operation: "READ", subscription: 1L, user: 1L, stream: "stream-2", endsAt: date],
			[operation: "READ", subscription: 1L, user: 1L, stream: "stream-3", endsAt: date],
			[operation: "READ", subscription: 2L, user: 2L, stream: "stream-2", endsAt: date],
			[operation: "READ", subscription: 2L, user: 2L, stream: "stream-3", endsAt: date],
		] as Set
	}
}
