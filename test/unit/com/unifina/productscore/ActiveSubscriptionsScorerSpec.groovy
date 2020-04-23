package com.unifina.productscore

import com.unifina.domain.marketplace.FreeSubscription
import com.unifina.domain.marketplace.PaidSubscription
import com.unifina.domain.marketplace.Product
import com.unifina.domain.marketplace.SubscriptionStore
import com.unifina.domain.marketplace.productscore.ActiveSubscriptionsScorer
import com.unifina.domain.marketplace.productscore.ProductScorer
import com.unifina.domain.security.SecUser
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([Product, PaidSubscription, FreeSubscription])
class ActiveSubscriptionsScorerSpec extends Specification {
	ProductScorer scorer
	Product p

	void setup() {
		scorer = new ActiveSubscriptionsScorer(new SubscriptionStore())
		p = new Product()
	}

	void "active subscriptions scorer"() {
		setup:
		SecUser owner = new SecUser(
			username: "owner",
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
		when:
		int score = scorer.score(p)
		then:
		score == 5
	}
}
