package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "remove-serialization-1") {
		dropForeignKeyConstraint(baseTableName: "canvas", constraintName: "FKAE7A755835F2A96E")
		dropColumn(tableName: "canvas", columnName: "serialization_id")
	}
	changeSet(author: "kkn", id: "remove-serialization-2") {
		dropTable(tableName: "serialization")
	}
}
