package core
databaseChangeLog = {

	changeSet(author: "admin (generated)", id: "1470681247023-1") {
		dropIndex(indexName: "username", tableName: "signup_invite")
	}
}
