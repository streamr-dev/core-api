package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "new-stream-fields-1") {
		addColumn(tableName: "stream") {
			column(name: "auto_configure", type: "bit") {
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "kkn", id: "new-stream-fields-2") {
		grailsChange {
			change {
				sql.execute('UPDATE stream SET auto_configure = true')
			}
		}
	}
	changeSet(author: "kkn", id: "new-stream-fields-3") {
		addNotNullConstraint(columnDataType: "bit", columnName: "auto_configure", defaultNullValue: "true", tableName: "stream")
	}

	changeSet(author: "kkn", id: "new-stream-fields-4") {
		addColumn(tableName: "stream") {
			column(name: "storage_days", type: "integer") {
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "kkn", id: "new-stream-fields-5") {
		grailsChange {
			change {
				sql.execute('UPDATE stream SET storage_days = 365')
			}
		}
	}
	changeSet(author: "kkn", id: "new-stream-fields-6") {
		addNotNullConstraint(columnDataType: "integer", columnName: "storage_days", defaultNullValue: 365, tableName: "stream")
	}
}
