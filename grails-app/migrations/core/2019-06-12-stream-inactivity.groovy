package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "stream-inactivity-1") {
		addColumn(tableName: "stream") {
			column(name: "inactivity_threshold_hours", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}
	changeSet(author: "kkn", id: "stream-inactivity-2") {
		grailsChange {
			change {
				sql.execute('update stream set inactivity_threshold_hours = 48 where inactivity_threshold_hours = 0')
			}
		}
	}
}
