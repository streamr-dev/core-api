package com.unifina.productscore

import com.unifina.domain.marketplace.Product
import com.unifina.domain.marketplace.productscore.ImageScorer
import com.unifina.domain.marketplace.productscore.ProductScorer
import spock.lang.Specification

class ImageScorerSpec extends Specification {
	ProductScorer scorer
	Product p

	void setup() {
		scorer = new ImageScorer()
		p = new Product()
	}

	def "product image scorer"(String imageURL, int expected) {
		setup:
		p.imageUrl = imageURL
		expect:
		scorer.score(p) == expected
		where:
		imageURL                           | expected
		""                                 | -10
		null                               | -10
		"http://www.pexels.com/image.jpeg" | 1
	}
}
