package com.unifina.domain

import grails.test.spock.IntegrationSpec
import grails.validation.ValidationException

class SubscriptionPaidIntegrationSpec extends IntegrationSpec {

	Product product

	void setup() {
		def owner = new User(
			username: "subscription-service-integration-spec-1@streamr.network",
			name: "Subscription Service Integration Spec 1",
		).save(failOnError: true)

		product = new Product(
			name: "Subscription Service Integration Spec Product",
			description: "description",
			category: Category.get("energy"),
			state: Product.State.DEPLOYED,
			dateCreated: new Date(),
			lastUpdated: new Date(),
			owner: owner,
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0x0000000000000000000000000000000000000000",
			pricePerSecond: 1,
		).save(failOnError: true)
	}

	void "fetchUser() is case-insensitive w.r.t. ethereum addresses [database property]"() {
		setup:
		def subscriber = new User(
			username: "0xffffffffffFFFFFFFFFFaaaaaaaaaaBBBBBBBBBB",
			name: "Subscription Service Integration Spec 2",
		).save(failOnError: true)
		def subscription = new SubscriptionPaid(
			product: product,
			endsAt: new Date(0),
			address: "0xffffffffffffffffffffaaaaaaaaaabbbbbbbbbb"
		)

		expect:
		subscription.fetchUser() == subscriber
	}

	void "fetching by address is case-insensitive w.r.t. ethereum addresses [database property]"() {
		setup:
		def subscription = new SubscriptionPaid(
			product: product,
			endsAt: new Date(0),
			address: "0xffffffffffffffffffffaaaaaaaaaabbbbbbbbbb"
		).save(failOnError: true)

		expect:
		SubscriptionPaid.findByProductAndAddress(product, "0xFFFFFFFFFFFFFFFFFFFFAAAAAAAAAAbbbbbbbbbb") == subscription
	}

	void "address-product uniqueness constraint is case-insensitive w.r.t. ethereum addresses [database property]"() {
		setup:
		new SubscriptionPaid(
			product: product,
			endsAt: new Date(0),
			address: "0xffffffffffffffffffffaaaaaaaaaabbbbbbbbbb"
		).save(failOnError: true)

		when:
		new SubscriptionPaid(
			product: product,
			endsAt: new Date(0),
			address: "0xFFFFFFFFFFFFFFFFFFFFaaaaaaaaaaBBBBBBBBBB"
		).save(failOnError: true)

		then:
		def e = thrown(ValidationException)
		e.message.contains("unique")
	}

}
