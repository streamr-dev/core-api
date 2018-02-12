package core

databaseChangeLog = {
	changeSet(author: "eric", id: "add-anonymous-index-to-permission") {
		createIndex(indexName: "anonymous_idx", tableName: "permission") {
			column(name: "anonymous")
		}
	}
}
