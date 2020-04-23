package com.unifina.domain.marketplace.productscore

import com.unifina.domain.marketplace.Product
import groovy.transform.CompileStatic

/**
 * Score points for product image.
 */
@CompileStatic
class ImageScorer implements ProductScorer {
	private static final int A = 1
	@Override
	int score(Product p) {
		int score = 1
		if (p.imageUrl == null || p.imageUrl == "") {
			score = -10
		} else {
			score = 1
		}
		score = score * A
		return score
	}
}
