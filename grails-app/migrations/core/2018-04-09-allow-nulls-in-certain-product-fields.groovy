package core
databaseChangeLog = {

	changeSet(author: "eric", id: "allow-nulls-in-certain-product-fields-1") {
		dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "beneficiary_address", tableName: "product")
	}

	changeSet(author: "harbu1 (generated)", id: "allow-nulls-in-certain-product-fields-2") {
		dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "owner_address", tableName: "product")
	}
}
