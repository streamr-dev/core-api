package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "rm-tour-user-1") {
		dropForeignKeyConstraint(baseTableName: "tour_user", constraintName: "FK2ED7F15260701D32")
	}
	changeSet(author: "kkn", id: "rm-tour-user-2") {
		dropTable(tableName: "tour_user")
	}
}
