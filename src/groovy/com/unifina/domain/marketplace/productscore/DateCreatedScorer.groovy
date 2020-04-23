package com.unifina.domain.marketplace.productscore

import com.unifina.domain.marketplace.Product
import groovy.transform.CompileStatic

/**
 * Novelty score. How new is the product?
 */
@CompileStatic
class DateCreatedScorer implements ProductScorer {
	private static final int A = 1

	@Override
	int score(Product p) {
		int score = 0
		Date now = new Date()
		if (p.isCreatedAfter(now.minus(8))) {
			score += 16
		} else if (p.isCreatedAfter(now.minus(31))) {
			score += 8
		} else if (p.isCreatedAfter(now.minus(91))) {
			score += 4
		} else if (p.isCreatedAfter(now.minus(181))) {
			score += 2
		} else if (p.isCreatedAfter(now.minus(366))) {
			score += 1
		} else { // products over 366 days old
		}
		score = score * A
		return score
	}
}
