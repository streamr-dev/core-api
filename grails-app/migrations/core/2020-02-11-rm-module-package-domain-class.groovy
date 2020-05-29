package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "rm-module-package-domain-class-1") {
		dropForeignKeyConstraint(baseTableName: "module", constraintName: "FKC04BA66C96E04B35")
	}
	changeSet(author: "kkn", id: "rm-module-package-domain-class-2") {
		dropForeignKeyConstraint(baseTableName: "module_category", constraintName: "FK1AD2C17196E04B35")
	}
	changeSet(author: "kkn", id: "rm-module-package-domain-class-3") {
		dropForeignKeyConstraint(baseTableName: "permission", constraintName: "FKE125C5CFFEDA9555")
	}
	changeSet(author: "kkn", id: "rm-module-package-domain-class-4") {
		dropColumn(columnName: "module_package_id", tableName: "module")
	}
	changeSet(author: "kkn", id: "rm-module-package-domain-class-5") {
		dropColumn(columnName: "module_package_id", tableName: "module_category")
	}
	changeSet(author: "kkn", id: "rm-module-package-domain-class-6") {
		grailsChange {
			change {
				sql.execute("delete from permission where module_package_id is not null")
			}
		}
	}
	changeSet(author: "kkn", id: "rm-module-package-domain-class-7") {
		dropColumn(columnName: "module_package_id", tableName: "permission")
	}
	changeSet(author: "kkn", id: "rm-module-package-domain-class-8") {
		dropTable(tableName: "module_package")
	}
}
