package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "user-refactor-username-add-email-1") {
		addColumn(tableName: "sec_user") {
			column(name: "email", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}
}
