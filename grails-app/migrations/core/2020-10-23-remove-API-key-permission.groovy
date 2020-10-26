package core
databaseChangeLog = {
	changeSet(author: "teogeb", id: "remove-API-key-permission-1") {
		dropForeignKeyConstraint(baseTableName: "permission", constraintName: "FKE125C5CF8EE35041")
	}
	changeSet(author: "teogeb", id: "remove-API-key-permission-2") {
		dropIndex(indexName: "FKE125C5CF8EE35041", tableName: "permission")
	}
	changeSet(author: "teogeb", id: "remove-API-key-permission-3") {
		dropColumn(columnName: "key_id", tableName: "permission")
	}
}
