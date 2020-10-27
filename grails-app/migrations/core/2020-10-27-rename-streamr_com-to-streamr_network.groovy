package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "rename-streamr_com-to-streamr_network", context: "test") {
		sql("update user set username = 'tester1@streamr.network', email = 'tester1@streamr.network' where username = 'tester1@streamr.com'")
		sql("update user set username = 'tester2@streamr.network', email = 'tester2@streamr.network' where username = 'tester2@streamr.com'")
		sql("update user set username = 'tester-admin@streamr.network', email = 'tester-admin@streamr.network' where username = 'tester-admin@streamr.com'")
		sql("update user set username = 'product-api-tester@streamr.network', email = 'product-api-tester@streamr.network' where username = 'product-api-tester@streamr.com'")
		sql("update user set username = 'product-api-tester-2@streamr.network', email = 'product-api-tester-2@streamr.network' where username = 'product-api-tester-2@streamr.com'")
		sql("update user set username = 'devops-user@streamr.network', email = 'devops-user@streamr.network' where username = 'devops-user@streamr.com'")
		sql("update user set username = 'stream-api-tester@streamr.network', email = 'stream-api-tester@streamr.network' where username = 'stream-api-tester@streamr.com'")
		sql("update user set username = 'stream-api-tester-2@streamr.network', email = 'stream-api-tester-2@streamr.network' where username = 'stream-api-tester-2@streamr.com'")
	}
}
