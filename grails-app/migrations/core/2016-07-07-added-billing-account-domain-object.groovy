package core
databaseChangeLog = {

	changeSet(author: "jarno (generated)", id: "1467894326900-1") {
		createTable(tableName: "billing_account") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "billing_accouPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "chargify_custemer_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "subscription", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "jarno (generated)", id: "1467894326900-2") {
		addColumn(tableName: "sec_user") {
			column(name: "billing_account_id", type: "bigint")
		}
	}

	changeSet(author: "jarno (generated)", id: "1467894326900-4") {
		createIndex(indexName: "FK375DF2F9AF8FDE9C", tableName: "sec_user") {
			column(name: "billing_account_id")
		}
	}

	changeSet(author: "jarno (generated)", id: "1467894326900-3") {
		addForeignKeyConstraint(baseColumnNames: "billing_account_id", baseTableName: "sec_user", constraintName: "FK375DF2F9AF8FDE9C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "billing_account", referencesUniqueColumn: "false")
	}
}
