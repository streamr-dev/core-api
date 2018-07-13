package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "rm-feed-start-on-demand-1") {
		dropColumn(columnName: "start_on_demand", tableName: "feed")
	}
}
