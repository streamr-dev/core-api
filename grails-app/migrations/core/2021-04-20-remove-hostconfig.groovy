package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "remove-hostconfig-1") {
		dropTable(tableName: "host_config")
	}
}
