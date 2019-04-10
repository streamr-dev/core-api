package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "rm-canvas-example-1") {
		dropColumn(columnName: "example", tableName: "canvas")
	}
}
