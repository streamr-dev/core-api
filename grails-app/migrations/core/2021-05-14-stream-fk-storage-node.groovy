package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "stream-fk-storage-node-1") {
		addForeignKeyConstraint(
			constraintName: "fk_stream_has_storage_nodes",
			baseTableName: "stream_storage_node",
			baseColumnNames: "stream_id",
			initiallyDeferred: "false",
			referencedColumnNames: "id",
			referencedTableName: "stream",
			referencesUniqueColumn: "false",
			deferrable: "false")
	}
}
