package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "rename-subscription-classes-2") {
		sql("update subscription set class = 'com.streamr.core.domain.SubscriptionFree' where class = 'com.unifina.domain.SubscriptionFree'");
		sql("update subscription set class = 'com.streamr.core.domain.SubscriptionPaid' where class = 'com.unifina.domain.SubscriptionPaid'");
	}
}
