package core

databaseChangeLog = {

	changeSet(author: "eric", id: "1452696039238-1") {
		addColumn(tableName: "saved_signal_path") {
			column(name: "uuid", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "eric", id: "1452696039238-2") {
		renameColumn(
			tableName: "stream",
			oldColumnName: "stream_config",
			newColumnName: "config",
			columnDataType: "longtext"
		)
	}

	changeSet(author: "eric", id: "1452696039238-4") {
		dropColumn(columnName: "api_secret", tableName: "sec_user")
	}

	changeSet(author: "eric", id: "1452696039238-3") {
		dropIndex(indexName: "localId_idx", tableName: "stream")
	}

	changeSet(author: "eric", id: "1452696039238-5") {
		dropColumn(columnName: "local_id", tableName: "stream")
	}
}
