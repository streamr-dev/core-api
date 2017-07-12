package core

databaseChangeLog = {

	changeSet(author: "haanpuu", id: "2016-07-11-dashboard-layout-field-type-change-1") {
		modifyDataType(tableName: "dashboard", columnName: "layout", newDataType: "text")
	}
}