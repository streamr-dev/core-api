package com.unifina.com.unifina.domain.marketplace

import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.PaidSubscription
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import grails.test.spock.IntegrationSpec
import grails.validation.ValidationException

class PaidSubscriptionIntegrationSpec extends IntegrationSpec {

	Product product

	void setup() {
		def owner = new SecUser(
			username: "subscription-service-integration-spec-1@streamr.com",
			password: "xxx",
			name: "Subscription Service Integration Spec 1",
			timezone: "UTC"
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
		def subscriber = new SecUser(
			username: "subscription-service-integration-spec-2@streamr.com",
			password: "xxx",
			name: "Subscription Service Integration Spec 2",
			timezone: "UTC"
		).save(failOnError: true)

		new IntegrationKey(
			user: subscriber,
			name: "Integration Key",
			service: IntegrationKey.Service.ETHEREUM_ID,
			json: "{}",
			idInService: "0xffffffffffFFFFFFFFFFaaaaaaaaaaBBBBBBBBBB"
		).save(failOnError: true)

		def subscription = new PaidSubscription(
			product: product,
			endsAt: new Date(0),
			address: "0xffffffffffffffffffffaaaaaaaaaabbbbbbbbbb"
		)

		expect:
		subscription.fetchUser() == subscriber
	}

	void "fetching by address is case-insensitive w.r.t. ethereum addresses [database property]"() {
		setup:
		def subscription = new PaidSubscription(
			product: product,
			endsAt: new Date(0),
			address: "0xffffffffffffffffffffaaaaaaaaaabbbbbbbbbb"
		).save(failOnError: true)

		expect:
		PaidSubscription.findByProductAndAddress(product, "0xFFFFFFFFFFFFFFFFFFFFAAAAAAAAAAbbbbbbbbbb") == subscription
	}

	void "address-product uniqueness constraint is case-insensitive w.r.t. ethereum addresses [database property]"() {
		setup:
		new PaidSubscription(
			product: product,
			endsAt: new Date(0),
			address: "0xffffffffffffffffffffaaaaaaaaaabbbbbbbbbb"
		).save(failOnError: true)

		when:
		new PaidSubscription(
			product: product,
			endsAt: new Date(0),
			address: "0xFFFFFFFFFFFFFFFFFFFFaaaaaaaaaaBBBBBBBBBB"
		).save(failOnError: true)

		then:
		def e = thrown(ValidationException)
		e.message.contains("unique")
	}

}
