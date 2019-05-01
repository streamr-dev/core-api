package core
databaseChangeLog = {
	changeSet(author: "mthambipillai", id: "add-Permission-parent-ref-1") {
		addColumn(tableName: "permission") {
			column(name: "parent_id", type: "bigint")
		}
	}
	changeSet(author: "mthambipillai", id: "add-Permission-parent-ref-4") {
		createIndex(indexName: "parent_idx", tableName: "permission") {
			column(name: "parent_id")
		}
	}
	changeSet(author: "mthambipillai", id: "add-Permission-parent-ref-3") {
		addForeignKeyConstraint(baseColumnNames: "parent_id", baseTableName: "permission", constraintName: "fk_parent", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "permission", referencesUniqueColumn: "false")
	}
}
