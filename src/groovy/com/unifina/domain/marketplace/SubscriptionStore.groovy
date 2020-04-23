package com.unifina.domain.marketplace

class SubscriptionStore extends Store<Subscription> {
	/**
	 * @see com.unifina.domain.marketplace.productscore.TotalSubscriptionsScorer
	 * @param p
	 * @return
	 */
	int findProductsTotalSubscriptionCount(Product p) {
		final int free = FreeSubscription.createCriteria().count() {
			eq("product", p)
		}
		final int paid = PaidSubscription.createCriteria().count() {
			eq("product", p)
		}
		final int result = free + paid
		return result
	}

	/**
	 * @see com.unifina.domain.marketplace.productscore.ActiveSubscriptionsScorer
	 * @param p
	 * @return
	 */
	int findProductsActiveSubscriptionCount(Product p) {
		final int free = FreeSubscription.createCriteria().count() {
			eq("product", p)
			gt("endsAt", new Date())
		}
		final int paid = PaidSubscription.createCriteria().count() {
			eq("product", p)
			gt("endsAt", new Date())
		}
		final int result = free + paid
		return result
	}
}
