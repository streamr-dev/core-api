package core
databaseChangeLog = {

	changeSet(author: "eric", id: "add-subscription-1") {
		createTable(tableName: "subscription") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "subscriptionPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "ends_at", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "product_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "address", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "eric", id: "add-subscription-2") {
		addColumn(tableName: "permission") {
			column(name: "subscription_id", type: "bigint")
		}
	}

	changeSet(author: "eric", id: "add-subscription-3") {
		createIndex(indexName: "subscription_idx", tableName: "permission") {
			column(name: "subscription_id")
		}
	}

	changeSet(author: "eric", id: "add-subscription-4") {
		createIndex(indexName: "product_idx", tableName: "subscription") {
			column(name: "product_id")
		}
	}

	changeSet(author: "eric", id: "add-subscription-5") {
		createIndex(indexName: "address_idx", tableName: "subscription") {
			column(name: "address")
		}
	}

	changeSet(author: "eric", id: "add-subscription-6") {
		createIndex(indexName: "unique_address_and_product_id", tableName: "subscription", unique: "true") {
			column(name: "product_id")
			column(name: "address")
		}
	}

	changeSet(author: "eric", id: "add-subscription-7") {
		addForeignKeyConstraint(
				baseColumnNames: "subscription_id",
				baseTableName: "permission",
				constraintName: "permission_to_subscription_fk",
				deferrable: "false",
				initiallyDeferred: "false",
				referencedColumnNames: "id",
				referencedTableName: "subscription",
				referencesUniqueColumn: "false"
		)
	}

	changeSet(author: "eric", id: "add-subscription-8") {
		addForeignKeyConstraint(
				baseColumnNames: "product_id",
				baseTableName: "subscription",
				constraintName: "subscription_to_product_fk",
				deferrable: "false",
				initiallyDeferred: "false",
				referencedColumnNames: "id",
				referencedTableName: "product",
				referencesUniqueColumn: "false"
		)
	}
}
