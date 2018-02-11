package core
databaseChangeLog = {

	changeSet(author: "hpihkala (generated)", id: "1518381756624-1") {
		createTable(tableName: "category") {
			column(name: "id", type: "varchar(255)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "categoryPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "default_image_url", type: "varchar(2048)")

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "hpihkala (generated)", id: "1518381756624-2") {
		createTable(tableName: "product") {
			column(name: "id", type: "varchar(255)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "productPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "beneficiary_address", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "category_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "longtext") {
				constraints(nullable: "false")
			}

			column(name: "image_url", type: "varchar(2048)")

			column(name: "last_updated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "minimum_subscription_in_seconds", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "owner_address", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "preview_config_json", type: "longtext")

			column(name: "preview_stream_id", type: "varchar(255)")

			column(name: "price_currency", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "price_per_second", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "state", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "tx", type: "varchar(255)")
		}
	}

	changeSet(author: "hpihkala (generated)", id: "1518381756624-3") {
		createTable(tableName: "product_stream") {
			column(name: "product_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "stream_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "hpihkala (generated)", id: "1518381756624-4") {
		addPrimaryKey(columnNames: "product_id, stream_id", constraintName: "product_streaPK", tableName: "product_stream")
	}

	changeSet(author: "hpihkala (generated)", id: "1518381756624-9") {
		createIndex(indexName: "FKED8DCCEF6F883B12", tableName: "product") {
			column(name: "category_id")
		}
	}

	changeSet(author: "hpihkala (generated)", id: "1518381756624-10") {
		createIndex(indexName: "FKED8DCCEF8DC84392", tableName: "product") {
			column(name: "preview_stream_id")
		}
	}

	changeSet(author: "hpihkala (generated)", id: "1518381756624-11") {
		createIndex(indexName: "beneficiary_address_idx", tableName: "product") {
			column(name: "beneficiary_address")
		}
	}

	changeSet(author: "hpihkala (generated)", id: "1518381756624-12") {
		createIndex(indexName: "owner_address_idx", tableName: "product") {
			column(name: "owner_address")
		}
	}

	changeSet(author: "hpihkala (generated)", id: "1518381756624-13") {
		createIndex(indexName: "FK112E4D086527F49", tableName: "product_stream") {
			column(name: "stream_id")
		}
	}

	changeSet(author: "hpihkala (generated)", id: "1518381756624-14") {
		createIndex(indexName: "FK112E4D0FBBF2242", tableName: "product_stream") {
			column(name: "product_id")
		}
	}

	changeSet(author: "hpihkala (generated)", id: "1518381756624-5") {
		addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "product", constraintName: "FKED8DCCEF6F883B12", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "category", referencesUniqueColumn: "false")
	}

	changeSet(author: "hpihkala (generated)", id: "1518381756624-6") {
		addForeignKeyConstraint(baseColumnNames: "preview_stream_id", baseTableName: "product", constraintName: "FKED8DCCEF8DC84392", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "stream", referencesUniqueColumn: "false")
	}

	changeSet(author: "hpihkala (generated)", id: "1518381756624-7") {
		addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_stream", constraintName: "FK112E4D0FBBF2242", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "product", referencesUniqueColumn: "false")
	}

	changeSet(author: "hpihkala (generated)", id: "1518381756624-8") {
		addForeignKeyConstraint(baseColumnNames: "stream_id", baseTableName: "product_stream", constraintName: "FK112E4D086527F49", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "stream", referencesUniqueColumn: "false")
	}
}
