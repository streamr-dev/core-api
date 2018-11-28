package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "unify-domain-1") {
		dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "category_id", tableName: "product")
	}
	changeSet(author: "kkn", id: "unify-domain-2") {
		modifyDataType(columnName: "description", newDataType: "longtext", tableName: "product")
	}
	changeSet(author: "kkn", id: "unify-domain-3") {
		dropNotNullConstraint(columnDataType: "longtext", columnName: "description", tableName: "product")
	}
}
