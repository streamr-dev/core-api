package core
databaseChangeLog = {

	changeSet(author: "aapeli", id: "create-account-integration-key") {
		createTable(tableName: "integration_key") {
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

			column(name: "service", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "aapeli", id: "create-account-integration-key-2") {
		addForeignKeyConstraint(baseColumnNames: "user_id",
			baseTableName: "integration_key",
			constraintName: "fk_user_integration_key",
			deferrable: "false",
			initiallyDeferred: "false",
			referencedColumnNames: "id",
			referencedTableName: "sec_user",
			referencesUniqueColumn: "false"
		)
	}
}