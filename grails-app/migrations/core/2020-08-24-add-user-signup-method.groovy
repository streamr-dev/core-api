package core
databaseChangeLog = {
	changeSet(author: "teogeb", id: "add-user-signup-method-1") {
		addColumn(tableName: "user") {
			column(name: "signup_method", value: "UNKNOWN", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}
}
