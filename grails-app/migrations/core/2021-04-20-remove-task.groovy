package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "remove-task-1") {
		dropTable(tableName: "task")
	}
}
