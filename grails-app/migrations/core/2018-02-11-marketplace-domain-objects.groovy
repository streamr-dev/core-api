package core
databaseChangeLog = {

	changeSet(author: "hpihkala (generated)", id: "marketplace-domain-objects-1") {
		createTable(tableName: "category") {
			column(name: "id", type: "varchar(255)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "categoryPK")
			}

			column(name: "image_url", type: "varchar(2048)")

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "hpihkala (generated)", id: "marketplace-domain-objects-2") {
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

			column(name: "block_index", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "block_number", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "hpihkala (generated)", id: "marketplace-domain-objects-3") {
		createTable(tableName: "product_streams") {
			column(name: "stream_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "product_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "hpihkala (generated)", id: "marketplace-domain-objects-4") {
		addColumn(tableName: "permission") {
			column(name: "product_id", type: "varchar(255)")
		}
	}

	changeSet(author: "hpihkala (generated)", id: "marketplace-domain-objects-5") {
		addPrimaryKey(columnNames: "product_id, stream_id", tableName: "product_streams")
	}

	changeSet(author: "hpihkala (generated)", id: "marketplace-domain-objects-11") {
		createIndex(indexName: "fk_permission_product", tableName: "permission") {
			column(name: "product_id")
		}
	}

	changeSet(author: "hpihkala (generated)", id: "marketplace-domain-objects-12") {
		createIndex(indexName: "fk_product_category", tableName: "product") {
			column(name: "category_id")
		}
	}

	changeSet(author: "hpihkala (generated)", id: "marketplace-domain-objects-13") {
		createIndex(indexName: "fk_product_previewstream", tableName: "product") {
			column(name: "preview_stream_id")
		}
	}

	changeSet(author: "hpihkala (generated)", id: "marketplace-domain-objects-14") {
		createIndex(indexName: "beneficiary_address_idx", tableName: "product") {
			column(name: "beneficiary_address")
		}
	}

	changeSet(author: "hpihkala (generated)", id: "marketplace-domain-objects-15") {
		createIndex(indexName: "owner_address_idx", tableName: "product") {
			column(name: "owner_address")
		}
	}

	changeSet(author: "hpihkala (generated)", id: "marketplace-domain-objects-16") {
		createIndex(indexName: "fk_productstreams_stream", tableName: "product_streams") {
			column(name: "stream_id")
		}
	}

	changeSet(author: "hpihkala (generated)", id: "marketplace-domain-objects-17") {
		createIndex(indexName: "fk_productstreams_product", tableName: "product_streams") {
			column(name: "product_id")
		}
	}

	changeSet(author: "hpihkala (generated)", id: "marketplace-domain-objects-6") {
		addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "permission", constraintName: "fk_permission_product", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "product", referencesUniqueColumn: "false")
	}

	changeSet(author: "hpihkala (generated)", id: "marketplace-domain-objects-7") {
		addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "product", constraintName: "fk_product_category", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "category", referencesUniqueColumn: "false")
	}

	changeSet(author: "hpihkala (generated)", id: "marketplace-domain-objects-8") {
		addForeignKeyConstraint(baseColumnNames: "preview_stream_id", baseTableName: "product", constraintName: "fk_product_previewstream", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "stream", referencesUniqueColumn: "false")
	}

	changeSet(author: "hpihkala (generated)", id: "marketplace-domain-objects-9") {
		addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_streams", constraintName: "fk_productstreams_product", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "product", referencesUniqueColumn: "false")
	}

	changeSet(author: "hpihkala (generated)", id: "marketplace-domain-objects-10") {
		addForeignKeyConstraint(baseColumnNames: "stream_id", baseTableName: "product_streams", constraintName: "fk_productstreams_stream", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "stream", referencesUniqueColumn: "false")
	}
}
