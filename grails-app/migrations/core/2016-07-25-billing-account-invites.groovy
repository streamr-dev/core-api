package core
databaseChangeLog = {

	changeSet(author: "jarno (generated)", id: "1469440171211-1") {
		createTable(tableName: "billing_account_invite") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "billing_accouPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "token", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "used", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "jarno (generated)", id: "1469440171211-2") {
		createTable(tableName: "billing_account_invite_billing_account") {
			column(name: "billing_account_invite_billing_account_id", type: "bigint")

			column(name: "billing_account_id", type: "bigint")
		}
	}

	changeSet(author: "jarno (generated)", id: "1469440171211-3") {
		createTable(tableName: "billing_account_invite_sec_user") {
			column(name: "billing_account_invite_users_id", type: "bigint")

			column(name: "sec_user_id", type: "bigint")
		}
	}

	changeSet(author: "jarno (generated)", id: "1469440171211-4") {
		addColumn(tableName: "billing_account") {
			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "jarno (generated)", id: "1469440171211-9") {
		createIndex(indexName: "FK365CCBC953473E25", tableName: "billing_account_invite_billing_account") {
			column(name: "billing_account_invite_billing_account_id")
		}
	}

	changeSet(author: "jarno (generated)", id: "1469440171211-10") {
		createIndex(indexName: "FK365CCBC9AF8FDE9C", tableName: "billing_account_invite_billing_account") {
			column(name: "billing_account_id")
		}
	}

	changeSet(author: "jarno (generated)", id: "1469440171211-11") {
		createIndex(indexName: "FK8DDEDB99612BC826", tableName: "billing_account_invite_sec_user") {
			column(name: "billing_account_invite_users_id")
		}
	}

	changeSet(author: "jarno (generated)", id: "1469440171211-12") {
		createIndex(indexName: "FK8DDEDB99872C9F44", tableName: "billing_account_invite_sec_user") {
			column(name: "sec_user_id")
		}
	}

	changeSet(author: "jarno (generated)", id: "1469440171211-5") {
		addForeignKeyConstraint(baseColumnNames: "billing_account_id", baseTableName: "billing_account_invite_billing_account", constraintName: "FK365CCBC9AF8FDE9C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "billing_account", referencesUniqueColumn: "false")
	}

	changeSet(author: "jarno (generated)", id: "1469440171211-6") {
		addForeignKeyConstraint(baseColumnNames: "billing_account_invite_billing_account_id", baseTableName: "billing_account_invite_billing_account", constraintName: "FK365CCBC953473E25", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "billing_account_invite", referencesUniqueColumn: "false")
	}

	changeSet(author: "jarno (generated)", id: "1469440171211-7") {
		addForeignKeyConstraint(baseColumnNames: "billing_account_invite_users_id", baseTableName: "billing_account_invite_sec_user", constraintName: "FK8DDEDB99612BC826", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "billing_account_invite", referencesUniqueColumn: "false")
	}

	changeSet(author: "jarno (generated)", id: "1469440171211-8") {
		addForeignKeyConstraint(baseColumnNames: "sec_user_id", baseTableName: "billing_account_invite_sec_user", constraintName: "FK8DDEDB99872C9F44", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "sec_user", referencesUniqueColumn: "false")
	}
}
