package com.unifina.productscore

import com.unifina.domain.marketplace.Product
import com.unifina.domain.marketplace.productscore.DescriptionScorer
import com.unifina.domain.marketplace.productscore.ProductScorer
import spock.lang.Specification

class DescriptionScorerSpec extends Specification {
	ProductScorer scorer
	Product p

	void setup() {
		scorer = new DescriptionScorer()
		p = new Product()
	}

	def "product description scorer"(String description, int expected) {
		setup:
		p.description = description
		expect:
		scorer.score(p) == expected
		where:
		description                                   | expected
		""                                            | -20
		null                                          | -20
		String.join("", Collections.nCopies(30, "x")) | 1
	}
}
