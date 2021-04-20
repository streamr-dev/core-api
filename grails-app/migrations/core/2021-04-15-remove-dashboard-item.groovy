package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "remove-dashboard-item-1") {
		dropTable(tableName: "dashboard_item")
	}
}
