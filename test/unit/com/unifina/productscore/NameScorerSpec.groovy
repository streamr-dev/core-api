package com.unifina.productscore

import com.unifina.domain.marketplace.Product
import com.unifina.domain.marketplace.productscore.NameScorer
import com.unifina.domain.marketplace.productscore.ProductScorer
import spock.lang.Specification

class NameScorerSpec extends Specification {
	ProductScorer scorer
	Product p

	void setup() {
		scorer = new NameScorer()
		p = new Product()
	}

	def "product name scorer"(String name, int expected) {
		setup:
		p.name = name
		expect:
		scorer.score(p) == expected
		where:
		name                     | expected
		Product.DEFAULT_NAME     | -20
		"product has been named" | 1
	}
}
