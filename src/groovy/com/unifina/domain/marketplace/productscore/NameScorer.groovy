package com.unifina.domain.marketplace.productscore

import com.unifina.domain.marketplace.Product
import groovy.transform.CompileStatic

/**
 * Score based on products name.
 */
@CompileStatic
class NameScorer implements ProductScorer {
	private static final int A = 1
	@Override
	int score(Product p) {
		int score = 0
		if (p.name == Product.DEFAULT_NAME) {
			score = -20
		} else {
			score = 1
		}
		score = score * A
		return score
	}
}
