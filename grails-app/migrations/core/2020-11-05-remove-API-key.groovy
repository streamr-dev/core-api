package core
databaseChangeLog = {
	changeSet(author: "teogeb", id: "remove-API-key-1") {
		dropTable(tableName: "key")
	}
}
