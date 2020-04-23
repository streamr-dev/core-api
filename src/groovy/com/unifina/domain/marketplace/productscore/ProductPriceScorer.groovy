package com.unifina.domain.marketplace.productscore

import com.unifina.domain.marketplace.Product
import groovy.transform.CompileStatic

/**
 * Score based on whether product is free or paid.
 */
@CompileStatic
class ProductPriceScorer implements ProductScorer {
	private static final int A = 1
	@Override
	int score(Product p) {
		int score = 1
		if (p.isFree()) {
			score = 0
		}
		score = score * A
		return score
	}
}
