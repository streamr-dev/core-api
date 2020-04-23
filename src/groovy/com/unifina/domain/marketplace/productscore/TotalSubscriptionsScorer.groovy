package com.unifina.domain.marketplace.productscore

import com.unifina.domain.marketplace.Product
import com.unifina.domain.marketplace.SubscriptionStore
import groovy.transform.CompileStatic

/**
 * Score based on number of total subscriptions.
 */
@CompileStatic
class TotalSubscriptionsScorer implements ProductScorer {
	private static final int A = 1
	private final SubscriptionStore store

	TotalSubscriptionsScorer(SubscriptionStore store) {
		this.store = store
	}

	@Override
	int score(Product p) {
		int score = store.findProductsTotalSubscriptionCount(p)
		score = score * A
		return score
	}
}
