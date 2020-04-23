package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "add-product-score_mod-1") {
		addColumn(tableName: "product") {
			column(name: "score_mod", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}
	changeSet(author: "kkn", id: "add-product-score_mod-2") {
		modifyDataType(columnName: "terms_of_use_commercial_use", newDataType: "bit", tableName: "product")
	}
	changeSet(author: "kkn", id: "add-product-score_mod-3") {
		dropNotNullConstraint(columnDataType: "bit", columnName: "terms_of_use_commercial_use", tableName: "product")
	}
	changeSet(author: "kkn", id: "add-product-score_mod-4") {
		modifyDataType(columnName: "terms_of_use_redistribution", newDataType: "bit", tableName: "product")
	}
	changeSet(author: "kkn", id: "add-product-score_mod-5") {
		dropNotNullConstraint(columnDataType: "bit", columnName: "terms_of_use_redistribution", tableName: "product")
	}
	changeSet(author: "kkn", id: "add-product-score_mod-6") {
		modifyDataType(columnName: "terms_of_use_reselling", newDataType: "bit", tableName: "product")
	}
	changeSet(author: "kkn", id: "add-product-score_mod-7") {
		dropNotNullConstraint(columnDataType: "bit", columnName: "terms_of_use_reselling", tableName: "product")
	}
	changeSet(author: "kkn", id: "add-product-score_mod-8") {
		modifyDataType(columnName: "terms_of_use_storage", newDataType: "bit", tableName: "product")
	}
	changeSet(author: "kkn", id: "add-product-score_mod-9") {
		dropNotNullConstraint(columnDataType: "bit", columnName: "terms_of_use_storage", tableName: "product")
	}
}
