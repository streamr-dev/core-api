package core

databaseChangeLog = {
	changeSet(author: "jtakalai", id: "1455118124727-1") {
		dropTable(tableName: "feed_user")
	}

	changeSet(author: "jtakalai", id: "1455118124727-2") {
		dropTable(tableName: "module_package_user")
	}
}
