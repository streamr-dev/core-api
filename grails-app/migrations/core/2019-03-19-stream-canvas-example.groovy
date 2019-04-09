package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "stream-canvas-example-1") {
		addColumn(tableName: "canvas") {
			column(name: "example_type", type: "integer", defaultValue: 0) {
				constraints(nullable: "false")
			}
		}
		createIndex(indexName: "example_type_idx", tableName: "canvas") {
			column(name: "example_type")
		}
	}
	changeSet(author: "kkn", id: "stream-canvas-example-2") {
		addColumn(tableName: "stream") {
			column(name: "example_type", type: "integer", defaultValue: 0) {
				constraints(nullable: "false")
			}
		}
		createIndex(indexName: "example_type_idx", tableName: "stream") {
			column(name: "example_type")
		}
	}
}
