package core
databaseChangeLog = {

	changeSet(author: "jarno (generated)", id: "1468491374896-1") {
		addColumn(tableName: "billing_account") {
			column(name: "api_key", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}
}
