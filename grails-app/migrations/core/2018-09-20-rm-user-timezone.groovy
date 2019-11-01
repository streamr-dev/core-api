package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "rm-user-timezone-1") {
		dropColumn(columnName: "timezone", tableName: "sec_user")
	}
}
