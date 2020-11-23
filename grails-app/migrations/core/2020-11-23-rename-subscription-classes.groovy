package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "rename-subscription-classes-1") {
		sql("update subscription set class = 'com.unifina.domain.SubscriptionFree' where class = 'com.unifina.domain.FreeSubscription'");
		sql("update subscription set class = 'com.unifina.domain.SubscriptionPaid' where class = 'com.unifina.domain.PaidSubscription'");
	}
}
