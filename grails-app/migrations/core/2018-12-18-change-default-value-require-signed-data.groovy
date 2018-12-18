package core
databaseChangeLog = {
	changeSet(author: "mthambipillai", id: "change-default-value-require-signed-data-1") {
		modifyDataType(columnName: "require_signed_data", newDataType: "bit", tableName: "stream")
	}
	changeSet(author: "mthambipillai", id: "change-default-value-require-signed-data-2") {
		addNotNullConstraint(columnDataType: "bit", columnName: "require_signed_data", defaultNullValue: "0", tableName: "stream")
	}
}
