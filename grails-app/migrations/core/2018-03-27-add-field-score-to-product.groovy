package core
databaseChangeLog = {

	changeSet(author: "eric", id: "add-field-score-to-product-1") {
		addColumn(tableName: "product") {
			column(name: "score", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "eric", id: "add-field-score-to-product-2") {
		createIndex(indexName: "score_idx", tableName: "product") {
			column(name: "score")
		}
	}
}
