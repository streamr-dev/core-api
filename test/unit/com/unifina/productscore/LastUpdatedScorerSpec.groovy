package com.unifina.productscore

import com.unifina.domain.marketplace.Product
import com.unifina.domain.marketplace.productscore.LastUpdatedScorer
import com.unifina.domain.marketplace.productscore.ProductScorer
import spock.lang.Specification

class LastUpdatedScorerSpec extends Specification {
	ProductScorer scorer
	Product p

	void setup() {
		scorer = new LastUpdatedScorer()
		p = new Product()
	}

	def "product last updated scorer"(Date lastUpdated, int expected) {
		setup:
		p.lastUpdated = lastUpdated
		expect:
		scorer.score(p) == expected
		where:
		lastUpdated           | expected
		new Date().minus(365) | 0
		new Date().minus(181) | 0
		new Date().minus(180) | 1
		new Date().minus(90)  | 1
		new Date().minus(30)  | 1
		new Date().minus(7)   | 1
	}
}
