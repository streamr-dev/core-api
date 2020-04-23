package com.unifina.productscore

import com.unifina.domain.marketplace.Product
import com.unifina.domain.marketplace.productscore.DateCreatedScorer
import com.unifina.domain.marketplace.productscore.ProductScorer
import spock.lang.Specification

class DateCreatedScorerSpec extends Specification {
	ProductScorer scorer
	Product p

	void setup() {
		scorer = new DateCreatedScorer()
		p = new Product()
	}

	void "product date created scorer"(Date dateCreated, int expected) {
		setup:
		p.dateCreated = dateCreated
		expect:
		scorer.score(p) == expected
		where:
		dateCreated           | expected
		new Date().minus(366) | 0
		new Date().minus(365) | 1
		new Date().minus(181) | 1
		new Date().minus(180) | 2
		new Date().minus(179) | 2
		new Date().minus(91)  | 2
		new Date().minus(90)  | 4
		new Date().minus(31)  | 4
		new Date().minus(30)  | 8
		new Date().minus(8)   | 8
		new Date().minus(7)   | 16
		new Date().minus(6)   | 16
		new Date()            | 16
	}
}
