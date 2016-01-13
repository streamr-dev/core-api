databaseChangeLog = {

	changeSet(author: "jtakalai (generated)", id: "1452674923112-1") {
		createTable(tableName: "permission") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "permissionPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "clazz", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "long_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "operation", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "string_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "jtakalai (generated)", id: "1452674923112-3") {
		createIndex(indexName: "FKE125C5CF60701D32", tableName: "permission") {
			column(name: "user_id")
		}
	}

	changeSet(author: "jtakalai (generated)", id: "1452674923112-2") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "permission", constraintName: "FKE125C5CF60701D32", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "sec_user", referencesUniqueColumn: "false")
	}


}
