package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "add-owner-to-product-1") {
		addColumn(tableName: "product") {
			column(name: "owner", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}
}
