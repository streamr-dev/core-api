package com.streamr.core.service

import com.streamr.core.BeanMockingSpecification
import com.streamr.core.domain.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(SubscriptionService)
@Mock([User, SubscriptionFree, SubscriptionPaid, Product, Subscription])
class SubscriptionServiceSpec extends BeanMockingSpecification {
	User user, user2
	String s1, s2, s3
	Product product

	EthereumUserService ethereumUserService

	void setup() {
		user = new User(username: "0x0000000000000000000000000000000000000005").save(failOnError: true, validate: false)
		user2 = new User(username: "0x000000000000000000000000000000000000000C").save(failOnError: true, validate: false)
		s1 = "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF/s1"
		s2 = "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF/s2"
		s3 = "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF/s3"
		product = new Product(streams: [s1, s2]).save(failOnError: true, validate: false)
		ethereumUserService = mockBean(EthereumUserService, Mock(EthereumUserService))
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

		when:
		def s = service.subscribeToFreeProduct(product, user, new Date())

		then:
		s.id != null
		SubscriptionFree.findAll() == [s]
	}

	void "subscribeToFreeProduct() updates existing Subscription if product-user exists"() {
		product.pricePerSecond = 0

		Subscription sub1 = new SubscriptionFree(product: product, user: user, endsAt: new Date()).save(failOnError: true, validate: false)

		when:
		def newDate = new Date()
		def sub2 = service.subscribeToFreeProduct(product, user, newDate)

		then:
		sub2.id == sub1.id
		Subscription.count() == 1
		Subscription.findById(sub2.id).endsAt == newDate
	}
}
