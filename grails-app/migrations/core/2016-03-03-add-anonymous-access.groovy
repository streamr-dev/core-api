package core

databaseChangeLog = {

	changeSet(author: "jtakalai (generated)", id: "1457014368299-1") {
		addColumn(tableName: "permission") {
			column(name: "anonymous", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}
}
