package core
databaseChangeLog = {

	changeSet(author: "jarno (generated)", id: "1470901118790-1") {
		addColumn(tableName: "billing_account_invite") {
			column(name: "billing_account_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "jarno (generated)", id: "1470901118790-2") {
		addColumn(tableName: "billing_account_invite") {
			column(name: "email", type: "varchar(255)")
		}
	}

	changeSet(author: "jarno (generated)", id: "1470901118790-3") {
		addColumn(tableName: "billing_account_invite") {
			column(name: "user_id", type: "bigint")
		}
	}

	changeSet(author: "jarno (generated)", id: "1470901118790-4") {
		dropForeignKeyConstraint(baseTableName: "billing_account_invite_billing_account", baseTableSchemaName: "core_dev_jarno", constraintName: "FK365CCBC9AF8FDE9C")
	}

	changeSet(author: "jarno (generated)", id: "1470901118790-5") {
		dropForeignKeyConstraint(baseTableName: "billing_account_invite_billing_account", baseTableSchemaName: "core_dev_jarno", constraintName: "FK365CCBC953473E25")
	}

	changeSet(author: "jarno (generated)", id: "1470901118790-6") {
		dropForeignKeyConstraint(baseTableName: "billing_account_invite_sec_user", baseTableSchemaName: "core_dev_jarno", constraintName: "FK8DDEDB99612BC826")
	}

	changeSet(author: "jarno (generated)", id: "1470901118790-7") {
		dropForeignKeyConstraint(baseTableName: "billing_account_invite_sec_user", baseTableSchemaName: "core_dev_jarno", constraintName: "FK8DDEDB99872C9F44")
	}

	changeSet(author: "jarno (generated)", id: "1470901118790-10") {
		createIndex(indexName: "FKAB79EB9F60701D32", tableName: "billing_account_invite") {
			column(name: "user_id")
		}
	}

	changeSet(author: "jarno (generated)", id: "1470901118790-11") {
		createIndex(indexName: "FKAB79EB9FAF8FDE9C", tableName: "billing_account_invite") {
			column(name: "billing_account_id")
		}
	}

	changeSet(author: "jarno (generated)", id: "1470901118790-12") {
		dropTable(tableName: "billing_account_invite_billing_account")
	}

	changeSet(author: "jarno (generated)", id: "1470901118790-13") {
		dropTable(tableName: "billing_account_invite_sec_user")
	}

	changeSet(author: "jarno (generated)", id: "1470901118790-8") {
		addForeignKeyConstraint(baseColumnNames: "billing_account_id", baseTableName: "billing_account_invite", constraintName: "FKAB79EB9FAF8FDE9C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "billing_account", referencesUniqueColumn: "false")
	}

	changeSet(author: "jarno (generated)", id: "1470901118790-9") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "billing_account_invite", constraintName: "FKAB79EB9F60701D32", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "sec_user", referencesUniqueColumn: "false")
	}
}
