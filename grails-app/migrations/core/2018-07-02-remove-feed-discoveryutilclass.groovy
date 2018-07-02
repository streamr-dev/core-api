package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "remove-feed-discoveryutilclass-1") {
		dropColumn(columnName: "discovery_util_class", tableName: "feed")
	}
}
