package core
databaseChangeLog = {

	changeSet(author: "aapeli", id: "create-account-table") {
		createTable(tableName: "account") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "VARCHAR(255)")

			column(name: "date_created", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "json", type: "VARCHAR(3000)")

			column(name: "user_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "type", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "aapeli", id: "create-account-table-2") {
		addForeignKeyConstraint(baseColumnNames: "user_id",
			baseTableName: "account",
			constraintName: "ACCOUNTUSERFOREIGNKEY",
			deferrable: "false",
			initiallyDeferred: "false",
			referencedColumnNames: "id",
			referencedTableName: "sec_user",
			referencesUniqueColumn: "false"
		)
	}
}