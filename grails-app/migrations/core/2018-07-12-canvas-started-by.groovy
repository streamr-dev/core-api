package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "canvas-started-by-1") {
		addColumn(tableName: "canvas") {
			column(name: "started_by_id", type: "bigint") {
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "kkn", id: "canvas-started-by-2") {
		createIndex(indexName: "started_by_id_idx", tableName: "canvas") {
			column(name: "started_by_id")
		}
	}
}
