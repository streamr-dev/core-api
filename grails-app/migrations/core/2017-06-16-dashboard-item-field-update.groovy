package core
databaseChangeLog = {

	changeSet(author: "aapeli (generated)", id: "2017-06-16-dashboard-item-field-update-1") {
		addNotNullConstraint(columnDataType: "varchar(255)", columnName: "dashboard_id", tableName: "dashboard_item")
	}

	changeSet(author: "aapeli (generated)", id: "2017-06-16-dashboard-item-field-update-3") {
		addNotNullConstraint(columnDataType: "longtext", columnName: "json", tableName: "integration_key")
	}

	changeSet(author: "aapeli (generated)", id: "2017-06-16-dashboard-item-field-update-4") {
		addNotNullConstraint(columnDataType: "varchar(255)", columnName: "name", tableName: "integration_key")
	}

	changeSet(author: "aapeli (generated)", id: "2017-06-16-dashboard-item-field-update-5") {
		dropColumn(columnName: "ord", tableName: "dashboard_item")
	}

	changeSet(author: "aapeli (generated)", id: "2017-06-16-dashboard-item-field-update-6") {
		dropColumn(columnName: "size", tableName: "dashboard_item")
	}
}
