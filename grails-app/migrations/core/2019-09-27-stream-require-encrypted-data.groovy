package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "stream-require-encrypted-data-1") {
		addColumn(tableName: "stream") {
			column(name: "require_encrypted_data", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}
}
