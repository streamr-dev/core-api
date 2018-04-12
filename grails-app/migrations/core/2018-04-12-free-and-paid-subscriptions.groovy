package core
databaseChangeLog = {
	changeSet(author: "eric", id: "free-and-paid-subscriptions-1") {
		addColumn(tableName: "subscription") {
			column(name: "class", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}
	changeSet(author: "eric", id: "free-and-paid-subscriptions-2") {
		addColumn(tableName: "subscription") {
			column(name: "user_id", type: "bigint")
		}
	}
	changeSet(author: "eric", id: "free-and-paid-subscriptions-3") {
		dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "address", tableName: "subscription")
	}
	changeSet(author: "eric", id: "free-and-paid-subscriptions-4") {
		createIndex(indexName: "user_idx", tableName: "subscription") {
			column(name: "user_id")
		}
	}
	changeSet(author: "eric", id: "free-and-paid-subscriptions-5") {
		createIndex(indexName: "unique_user_id_and_product_id", tableName: "subscription", unique: "true") {
			column(name: "user_id")
			column(name: "product_id")
		}
	}
	changeSet(author: "eric", id: "free-and-paid-subscriptions-6") {
		addForeignKeyConstraint(
			baseColumnNames: "user_id",
			baseTableName: "subscription",
			constraintName: "fk_subscription_user",
			deferrable: "false",
			initiallyDeferred: "false",
			referencedColumnNames: "id",
			referencedTableName: "sec_user",
			referencesUniqueColumn: "false"
		)
	}
}
