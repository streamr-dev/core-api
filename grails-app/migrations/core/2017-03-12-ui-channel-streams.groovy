package core
databaseChangeLog = {

	changeSet(author: "admin (generated)", id: "1489340226120-1") {
		addColumn(tableName: "stream") {
			column(name: "ui_channel", type: "bit", defaultValueBoolean: false) {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "admin (generated)", id: "1489340226120-2") {
		addColumn(tableName: "stream") {
			column(name: "ui_channel_canvas_id", type: "varchar(255)")
		}
	}

	changeSet(author: "admin (generated)", id: "1489340226120-3") {
		addColumn(tableName: "stream") {
			column(name: "ui_channel_path", type: "varchar(20000)")
		}
	}

	changeSet(author: "admin (generated)", id: "1489340226120-5") {
		createIndex(indexName: "FKCAD54F8052E2E25F", tableName: "stream") {
			column(name: "ui_channel_canvas_id")
		}
	}

	changeSet(author: "admin (generated)", id: "1489340226120-6") {
		createIndex(indexName: "ui_channel_path_idx", tableName: "stream") {
			column(name: "ui_channel_path")
		}
	}

	changeSet(author: "admin (generated)", id: "1489340226120-4") {
		addForeignKeyConstraint(baseColumnNames: "ui_channel_canvas_id", baseTableName: "stream", constraintName: "FKCAD54F8052E2E25F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "canvas", referencesUniqueColumn: "false")
	}
}
