package core
databaseChangeLog = {
	changeSet(author: "mthambipillai", id: "unique-ethereum-addresses-1") {
		createIndex(indexName: "id_in_service_uniq_1559553159638", tableName: "integration_key", unique: "true") {
			column(name: "id_in_service")
		}
	}
}
