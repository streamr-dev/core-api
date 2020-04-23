package com.unifina.domain.marketplace.productscore

import com.unifina.domain.marketplace.Product
import groovy.transform.CompileStatic

/**
 * Get points for recently updated products.
 */
@CompileStatic
class LastUpdatedScorer implements ProductScorer {
	private static final int A = 1

	@Override
	int score(Product p) {
		int score = 0
		Date now = new Date()
		if (p.isUpdatedAfter(now.minus(181))) {
			score += 1
		}
		score = score * A
		return score
	}
}
