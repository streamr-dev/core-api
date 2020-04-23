package com.unifina.domain.marketplace.productscore

import com.unifina.domain.marketplace.Product
import groovy.transform.CompileStatic

/**
 * Score based on amount of DATA earned by the product.
 */
@CompileStatic
class ProductsDATAAmountScorer implements ProductScorer {
	private static final int A = 1
	@Override
	int score(Product p) {
		int score = 0
		score = score * A
		return score
	}
}
