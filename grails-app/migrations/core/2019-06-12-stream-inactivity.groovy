package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "stream-inactivity-1") {
		addColumn(tableName: "stream") {
			column(name: "inactivity_threshold_hours", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}
}
