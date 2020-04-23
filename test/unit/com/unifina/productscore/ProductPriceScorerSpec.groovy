package com.unifina.productscore

import com.unifina.domain.marketplace.Product
import com.unifina.domain.marketplace.productscore.ProductPriceScorer
import com.unifina.domain.marketplace.productscore.ProductScorer
import spock.lang.Specification

class ProductPriceScorerSpec extends Specification {
	ProductScorer scorer
	Product p

	void setup() {
		scorer = new ProductPriceScorer()
		p = new Product()
	}

	def "product price scorer"(int pricePerSecond, int expected) {
		setup:
		p.pricePerSecond = pricePerSecond
		expect:
		scorer.score(p) == expected
		where:
		pricePerSecond | expected
		0              | 0
		1              | 1
	}
}
