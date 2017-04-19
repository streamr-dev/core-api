package core
databaseChangeLog = {

	changeSet(author: "eric", id: "add-key-domain-object-1") {
		createTable(tableName: "key") {
			column(name: "id", type: "varchar(255)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "keyPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint")
		}
	}

	changeSet(author: "eric", id: "add-key-domain-object-2") {
		addColumn(tableName: "permission") {
			column(name: "key_id", type: "varchar(255)")
		}
	}

	changeSet(author: "eric", id: "add-key-domain-object-3") {
		createIndex(indexName: "FKE125C5CF8EE35041", tableName: "permission") {
			column(name: "key_id")
		}
	}

	changeSet(author: "eric", id: "add-key-domain-object-4") {
		addForeignKeyConstraint(baseColumnNames: "user_id",
			baseTableName: "key",
			constraintName: "FK19E5F60701D32",
			deferrable: "false",
			initiallyDeferred: "false",
			referencedColumnNames: "id",
			referencedTableName: "sec_user",
			referencesUniqueColumn: "false"
		)
	}

	changeSet(author: "eric", id: "add-key-domain-object-5") {
		addForeignKeyConstraint(baseColumnNames: "key_id",
			baseTableName: "permission",
			constraintName: "FKE125C5CF8EE35041",
			deferrable: "false",
			initiallyDeferred:"false",
			referencedColumnNames: "id",
			referencedTableName: "key",
			referencesUniqueColumn: "false"
		)
	}
}
