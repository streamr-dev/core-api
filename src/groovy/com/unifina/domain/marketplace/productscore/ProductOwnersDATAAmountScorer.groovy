package com.unifina.domain.marketplace.productscore

import com.unifina.domain.marketplace.Product
import groovy.transform.CompileStatic

/**
 * Get points for Product owner's DATA amount.
 */
@CompileStatic
class ProductOwnersDATAAmountScorer implements ProductScorer {
	private static final int A = 1
	@Override
	int score(Product p) {
		int score = 0
		score = score * A
		return score
	}
}
