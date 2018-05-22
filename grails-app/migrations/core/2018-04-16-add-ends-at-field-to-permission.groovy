package core
databaseChangeLog = {
	changeSet(author: "eric", id: "add-ends-at-field-to-permission") {
		addColumn(tableName: "permission") {
			column(name: "ends_at", type: "datetime")
		}
	}
}
