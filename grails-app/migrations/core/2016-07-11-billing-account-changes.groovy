package core
databaseChangeLog = {

	changeSet(author: "jarno (generated)", id: "1468234842829-1") {
		addColumn(tableName: "billing_account") {
			column(name: "chargify_subscription_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "jarno (generated)", id: "1468234842829-2") {
		dropColumn(columnName: "subscription", tableName: "billing_account")
	}
}
