package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "product-pendingchanges-1") {
		addColumn(tableName: "product") {
			column(name: "pending_changes", type: "varchar(255)")
		}
	}
}
