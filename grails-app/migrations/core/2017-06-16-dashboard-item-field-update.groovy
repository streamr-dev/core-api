package core
databaseChangeLog = {

	changeSet(author: "aapeli (generated)", id: "1497621497496-1") {
		addNotNullConstraint(columnDataType: "varchar(255)", columnName: "dashboard_id", tableName: "dashboard_item")
	}

	changeSet(author: "aapeli (generated)", id: "1497621497496-2") {
		modifyDataType(columnName: "json", newDataType: "longtext", tableName: "integration_key")
	}

	changeSet(author: "aapeli (generated)", id: "1497621497496-3") {
		addNotNullConstraint(columnDataType: "longtext", columnName: "json", tableName: "integration_key")
	}

	changeSet(author: "aapeli (generated)", id: "1497621497496-4") {
		addNotNullConstraint(columnDataType: "varchar(255)", columnName: "name", tableName: "integration_key")
	}

	changeSet(author: "aapeli (generated)", id: "1497621497496-5") {
		dropColumn(columnName: "ord", tableName: "dashboard_item")
	}

	changeSet(author: "aapeli (generated)", id: "1497621497496-6") {
		dropColumn(columnName: "size", tableName: "dashboard_item")
	}
}
