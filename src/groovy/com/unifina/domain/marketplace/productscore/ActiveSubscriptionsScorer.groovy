package com.unifina.domain.marketplace.productscore

import com.unifina.domain.marketplace.Product
import com.unifina.domain.marketplace.SubscriptionStore
import groovy.transform.CompileStatic

/**
 * Number of active subscriptions score.
 */
@CompileStatic
class ActiveSubscriptionsScorer implements ProductScorer {
	private static final int A = 1
	private final SubscriptionStore store

	ActiveSubscriptionsScorer(SubscriptionStore store) {
		this.store = store
	}

	@Override
	int score(Product p) {
		int score = store.findProductsActiveSubscriptionCount(p)
		score = score * A
		return score
	}
}
