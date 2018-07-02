package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "rm-feed-bundled-feed-files-1") {
		dropColumn(columnName: "bundled_feed_files", tableName: "feed")
	}
}
