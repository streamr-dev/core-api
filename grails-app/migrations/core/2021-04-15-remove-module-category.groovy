package core

databaseChangeLog = {
	changeSet(author: "kkn", id: "remove-module-category-1") {
		dropForeignKeyConstraint(baseTableName: "module", constraintName: "FKC04BA66C28AB0672")
		dropColumn(tableName: "module", columnName: "category_id")
	}
	changeSet(author: "kkn", id: "remove-module-category-2") {
		dropTable(tableName: "module_category")
	}
}
