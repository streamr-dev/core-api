package core
databaseChangeLog = {
	changeSet(author: "hpihkala", id: "drop-feed-1") {
		dropForeignKeyConstraint(baseTableName: "feed", baseTableSchemaName: "core_dev", constraintName: "FK2FE59EB6140F06")
	}
	changeSet(author: "hpihkala", id: "drop-feed-2") {
		dropForeignKeyConstraint(baseTableName: "permission", baseTableSchemaName: "core_dev", constraintName: "FKE125C5CF72507A49")
	}
	changeSet(author: "hpihkala", id: "drop-feed-3") {
		dropForeignKeyConstraint(baseTableName: "stream", baseTableSchemaName: "core_dev", constraintName: "FKCAD54F8072507A49")
	}
	changeSet(author: "hpihkala", id: "drop-feed-4") {
		dropColumn(columnName: "feed_id", tableName: "permission")
	}
	changeSet(author: "hpihkala", id: "drop-feed-5") {
		dropColumn(columnName: "feed_id", tableName: "stream")
	}
	changeSet(author: "hpihkala", id: "drop-feed-6") {
		dropTable(tableName: "feed")
	}
}
