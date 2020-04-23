package com.unifina.domain.marketplace.productscore

import com.unifina.domain.marketplace.Product
import groovy.transform.CompileStatic

@CompileStatic
interface ProductScorer {
	/**
	 * Calculate score for product.
	 *
	 * @param p Product to score
	 * @return Score value
	 */
	int score(Product p)
}
