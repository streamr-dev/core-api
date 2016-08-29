package core
databaseChangeLog = {

	changeSet(author: "jarno (generated)", id: "1467895993957-1") {
		addColumn(tableName: "billing_account") {
			column(name: "chargify_customer_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "jarno (generated)", id: "1467895993957-2") {
		dropColumn(columnName: "chargify_custemer_id", tableName: "billing_account")
	}
}
