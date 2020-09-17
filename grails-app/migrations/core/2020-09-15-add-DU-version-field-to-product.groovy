package core
databaseChangeLog = {
	changeSet(author: "teogeb", id: "add-DU-version-field-to-product-1") {
		addColumn(tableName: "product") {
			column(name: "data_union_version", type: "integer")
		}
	}
	changeSet(author: "teogeb", id: "add-DU-version-field-to-product-2") {
		grailsChange {
			change {
				sql.execute("update product set data_union_version=1 where type=1")
			}
		}
	}
}
