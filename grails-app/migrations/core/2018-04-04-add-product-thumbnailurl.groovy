package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "add-product-thumbnailurl-1") {
		addColumn(tableName: "product") {
			column(name: "thumbnail_url", type: "varchar(2048)")
		}
	}
}
