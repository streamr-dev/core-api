package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "remove-user-password-1") {
		dropColumn(columnName: "password", tableName: "user")
	}
	changeSet(author: "kkn", id: "remove-user-password-2") {
		dropColumn(columnName: "password_expired", tableName: "user")
	}
}
