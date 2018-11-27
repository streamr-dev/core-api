package core
databaseChangeLog = {
	changeSet(author: "mthambipillai", id: "add-requireSignedData-field-1") {
		addColumn(tableName: "stream") {
			column(name: "require_signed_data", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}
	changeSet(author: "mthambipillai", id: "add-requireSignedData-field-3") {
		dropTable(tableName: "challenge")
	}
	changeSet(author: "mthambipillai", id: "add-requireSignedData-field-2") {
		addForeignKeyConstraint(baseColumnNames: "started_by_id", baseTableName: "canvas", constraintName: "FKAE7A7558BA6E1FE8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "sec_user", referencesUniqueColumn: "false")
	}
}
