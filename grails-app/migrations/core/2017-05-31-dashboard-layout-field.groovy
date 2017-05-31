package core

databaseChangeLog = {

	changeSet(author: "haanpuu", id: "2016-05-31-dashboard-layout-field-1") {
		addColumn(tableName: "dashboard") {
			column(name: "layout", type: "varchar(2000)")
		}
	}
}