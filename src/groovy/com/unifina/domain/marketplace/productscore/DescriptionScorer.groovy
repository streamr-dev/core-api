package com.unifina.domain.marketplace.productscore

import com.unifina.domain.marketplace.Product
import groovy.transform.CompileStatic

/**
 * Score based on products name.
 */
@CompileStatic
class DescriptionScorer implements ProductScorer {
	private static final int A = 1
	@Override
	int score(Product p) {
		int score = 0
		if (p.description == null || p.description == "") {
			score = -20
		} else if (p.description.length() >= 30) {
			score = 1
		}
		score = score * A
		return score
	}
}
