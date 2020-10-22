package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "fix-subscription-column-class-1") {
		sql("update subscription set class = 'com.unifina.domain.FreeSubscription' where class = 'com.unifina.domain.marketplace.FreeSubscription'");
		sql("update subscription set class = 'com.unifina.domain.PaidSubscription' where class = 'com.unifina.domain.marketplace.PaidSubscription'");
	}
}
