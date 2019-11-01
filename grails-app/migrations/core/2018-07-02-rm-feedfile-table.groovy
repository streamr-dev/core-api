package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "rm-feedfile-table-1") {
		dropTable(tableName: "feed_file")
	}
}
