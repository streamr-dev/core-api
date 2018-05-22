package core
databaseChangeLog = {
	changeSet(author: "eric", id: "add-devops-role") {
		insert(tableName: "sec_role") {
			column(name: "id", valueNumeric: 7)
			column(name: "version", valueNumeric: 0)
			column(name: "authority", value: "ROLE_DEV_OPS")
		}
	}
}