package core
databaseChangeLog = {

	changeSet(author: "aapeli (generated)", id: "1462796936141-1") {
		addColumn(tableName: "stream") {
			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "aapeli (generated)", id: "1462796936141-2") {
		addColumn(tableName: "stream") {
			column(name: "last_updated", type: "datetime") {
				constraints(nullable: "false")
			}
		}
	}

	// Undo change in "change-canvas-stream-modules-id-8"
	changeSet(author: "aapeli (generated)", id: "1462796936141-3") {
		dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "stream_id", tableName: "feed_file")
	}

	changeSet(author: "aapeli", id:"update-stream-dates-to-current-if-missing") {
		sql("update stream set date_created=CURRENT_TIMESTAMP, last_updated=CURRENT_TIMESTAMP where date_created = '0000-00-00 00:00:00'")
	}
}
