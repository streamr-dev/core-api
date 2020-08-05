package com.unifina.productscore

import com.unifina.domain.marketplace.FreeSubscription
import com.unifina.domain.marketplace.PaidSubscription
import com.unifina.domain.marketplace.Product
import com.unifina.domain.marketplace.productscore.ProductScorer
import com.unifina.domain.marketplace.productscore.TotalSubscriptionsScorer
import com.unifina.domain.security.SecUser
import com.unifina.domain.marketplace.SubscriptionStore
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([SecUser, Product, PaidSubscription, FreeSubscription])
class TotalSubscriptionsScorerSpec extends Specification {
	ProductScorer scorer
	Product p

	void setup() {
		scorer = new TotalSubscriptionsScorer(new SubscriptionStore())
		SecUser owner = new SecUser(
			username: "owner@example.com",
			password: "x",
			name: "product owner",
		)
		owner.save(validate: true, failOnError: true)
		p = new Product()
		p.owner = owner
		p.save(validate: true, failOnError: true)
		new PaidSubscription(
			address: "0x0000000000000000000000000000000000000000",
			endsAt: new Date().plus(100),
			product: p,
		).save(validate: true, failOnError: true)
		new PaidSubscription(
			address: "0x0000000000000000000000000000000000000000",
			endsAt: new Date().plus(100),
			product: p,
		).save(validate: true, failOnError: true)
		new PaidSubscription(
			address: "0x0000000000000000000000000000000000000000",
			endsAt: new Date().plus(100),
			product: p,
		).save(validate: true, failOnError: true)
		new FreeSubscription(
			endsAt: new Date().plus(100),
			product: p,
			user: owner,
		).save(validate: true, failOnError: true)
		new FreeSubscription(
			endsAt: new Date().plus(100),
			product: p,
			user: owner,
		).save(validate: true, failOnError: true)
	}

	def "total subscription scorer"() {
		when:
		int score = scorer.score(p)
		then:
		score == 5
	}
}
