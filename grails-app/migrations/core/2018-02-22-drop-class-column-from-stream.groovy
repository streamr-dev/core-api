package core

databaseChangeLog = {
	changeSet(author: "eric", id: "drop-class-column-from-stream") {
		dropColumn(columnName: "class", tableName: "stream")
	}
}
