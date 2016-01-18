package core
databaseChangeLog = {

	changeSet(author: "admin (generated)", id: "1452621480175-1") {
		createTable(tableName: "dashboard") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "VARCHAR(255)")

			column(name: "user_id", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-2") {
		createTable(tableName: "dashboard_item") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "dashboard_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "ord", type: "INT") {
				constraints(nullable: "false")
			}

			column(name: "size", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "title", type: "VARCHAR(255)")

			column(name: "ui_channel_id", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-3") {
		createTable(tableName: "feed") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "backtest_feed", type: "VARCHAR(255)")

			column(name: "bundled_feed_files", type: "BIT")

			column(name: "cache_class", type: "VARCHAR(255)")

			column(name: "cache_config", type: "VARCHAR(255)")

			column(name: "directory", type: "VARCHAR(255)")

			column(name: "discovery_util_class", type: "VARCHAR(255)")

			column(name: "discovery_util_config", type: "VARCHAR(255)")

			column(name: "event_recipient_class", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "feed_config", type: "VARCHAR(255)")

			column(name: "key_provider_class", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "message_source_class", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "message_source_config", type: "VARCHAR(255)")

			column(name: "module_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "parser_class", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "preprocessor", type: "VARCHAR(255)")

			column(name: "realtime_feed", type: "VARCHAR(255)")

			column(name: "start_on_demand", type: "BIT")

			column(name: "timezone", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-4") {
		createTable(tableName: "feed_file") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "begin_date", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "day", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "end_date", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "feed_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "format", type: "VARCHAR(255)")

			column(name: "name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "process_task_created", type: "BIT")

			column(name: "processed", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "processing", type: "BIT")

			column(name: "stream_id", type: "BIGINT")
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-5") {
		createTable(tableName: "feed_user") {
			column(name: "user_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "feed_id", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-6") {
		createTable(tableName: "host_config") {
			column(name: "host", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "parameter", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "value", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-7") {
		createTable(tableName: "module") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "category_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "implementing_class", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "js_module", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "hide", type: "BIT")

			column(name: "type", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "module_package_id", type: "BIGINT")

			column(name: "json_help", type: "LONGTEXT")

			column(name: "alternative_names", type: "VARCHAR(255)")

			column(name: "webcomponent", type: "VARCHAR(255)")
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-8") {
		createTable(tableName: "module_category") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "hide", type: "BIT")

			column(name: "module_package_id", type: "BIGINT")

			column(name: "name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "parent_id", type: "BIGINT")

			column(name: "sort_order", type: "INT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-9") {
		createTable(tableName: "module_package") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-10") {
		createTable(tableName: "module_package_user") {
			column(name: "user_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "module_package_id", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-11") {
		createTable(tableName: "registration_code") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "date_created", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "token", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "username", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-12") {
		createTable(tableName: "running_signal_path") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "adhoc", type: "BIT")

			column(name: "date_created", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "json", type: "LONGTEXT") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "request_url", type: "VARCHAR(255)")

			column(name: "runner", type: "VARCHAR(255)")

			column(name: "server", type: "VARCHAR(255)")

			column(name: "shared", type: "BIT")

			column(name: "state", type: "VARCHAR(255)")

			column(name: "user_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "serialized", type: "LONGTEXT")

			column(name: "serialization_time", type: "DATETIME")
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-13") {
		createTable(tableName: "saved_signal_path") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "has_exports", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "json", type: "LONGTEXT") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(defaultValueNumeric: "0", name: "type", type: "INT") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-14") {
		createTable(tableName: "sec_role") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "authority", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-15") {
		createTable(tableName: "sec_user") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "account_expired", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "account_locked", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "api_key", type: "VARCHAR(255)")

			column(name: "api_secret", type: "VARCHAR(255)")

			column(name: "enabled", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "password", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "password_expired", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "timezone", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "username", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-16") {
		createTable(tableName: "sec_user_sec_role") {
			column(name: "sec_role_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "sec_user_id", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-17") {
		createTable(tableName: "signup_invite") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "code", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "sent", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "used", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "username", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-18") {
		createTable(tableName: "stream") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "api_key", type: "VARCHAR(255)")

			column(name: "description", type: "VARCHAR(255)")

			column(name: "feed_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "first_historical_day", type: "DATETIME")

			column(name: "last_historical_day", type: "DATETIME")

			column(defaultValue: "", name: "local_id", type: "VARCHAR(255)")

			column(name: "name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "stream_config", type: "LONGTEXT")

			column(name: "user_id", type: "BIGINT")

			column(name: "uuid", type: "VARCHAR(255)")

			column(name: "class", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-19") {
		createTable(tableName: "task") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "available", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "category", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "complete", type: "BIT") {
				constraints(nullable: "false")
			}

			column(defaultValueNumeric: "0", name: "complexity", type: "INT") {
				constraints(nullable: "false")
			}

			column(name: "config", type: "VARCHAR(1000)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "error", type: "VARCHAR(1000)")

			column(name: "implementing_class", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(defaultValueNumeric: "0", name: "progress", type: "INT") {
				constraints(nullable: "false")
			}

			column(name: "run_after", type: "DATETIME")

			column(name: "server_ip", type: "VARCHAR(255)")

			column(name: "skip", type: "BIT")

			column(name: "status", type: "VARCHAR(255)")

			column(name: "task_group_id", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "BIGINT")
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-20") {
		createTable(tableName: "tour_user") {
			column(name: "user_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "tour_number", type: "INT") {
				constraints(nullable: "false")
			}

			column(name: "completed_at", type: "DATETIME") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-21") {
		createTable(tableName: "ui_channel") {
			column(name: "id", type: "VARCHAR(255)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "hash", type: "VARCHAR(255)")

			column(name: "module_id", type: "BIGINT")

			column(name: "name", type: "VARCHAR(255)")

			column(name: "running_signal_path_id", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-22") {
		addPrimaryKey(columnNames: "user_id, feed_id", tableName: "feed_user")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-23") {
		addPrimaryKey(columnNames: "host, parameter", tableName: "host_config")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-24") {
		addPrimaryKey(columnNames: "user_id, module_package_id", tableName: "module_package_user")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-25") {
		addPrimaryKey(columnNames: "sec_role_id, sec_user_id", tableName: "sec_user_sec_role")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-26") {
		addPrimaryKey(columnNames: "user_id, tour_number", tableName: "tour_user")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-52") {
		createIndex(indexName: "runner_idx", tableName: "running_signal_path", unique: "false") {
			column(name: "runner")
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-53") {
		createIndex(indexName: "authority", tableName: "sec_role", unique: "true") {
			column(name: "authority")
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-54") {
		createIndex(indexName: "apiKey_index", tableName: "sec_user", unique: "false") {
			column(name: "api_key")
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-55") {
		createIndex(indexName: "api_key_uniq_1452614412412", tableName: "sec_user", unique: "true") {
			column(name: "api_key")
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-56") {
		createIndex(indexName: "username", tableName: "sec_user", unique: "true") {
			column(name: "username")
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-57") {
		createIndex(indexName: "code", tableName: "signup_invite", unique: "true") {
			column(name: "code")
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-58") {
		createIndex(indexName: "username", tableName: "signup_invite", unique: "true") {
			column(name: "username")
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-59") {
		createIndex(indexName: "localId_idx", tableName: "stream", unique: "false") {
			column(name: "local_id")
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-60") {
		createIndex(indexName: "name_idx", tableName: "stream", unique: "false") {
			column(name: "name")
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-61") {
		createIndex(indexName: "uuid_idx", tableName: "stream", unique: "false") {
			column(name: "uuid")
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-62") {
		createIndex(indexName: "available_idx", tableName: "task", unique: "false") {
			column(name: "available")
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-63") {
		createIndex(indexName: "task_group_id_idx", tableName: "task", unique: "false") {
			column(name: "task_group_id")
		}
	}

	changeSet(author: "admin (generated)", id: "1452621480175-27") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "dashboard", /* baseTableSchemaName: "core_dev", */constraintName: "FKC18AEA9460701D32", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sec_user", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-28") {
		addForeignKeyConstraint(baseColumnNames: "dashboard_id", baseTableName: "dashboard_item", /* baseTableSchemaName: "core_dev", */constraintName: "FKF4B0C5DE70E281EB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "dashboard", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-29") {
		addForeignKeyConstraint(baseColumnNames: "ui_channel_id", baseTableName: "dashboard_item", /* baseTableSchemaName: "core_dev", */constraintName: "FKF4B0C5DE8A8883E5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "ui_channel", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-30") {
		addForeignKeyConstraint(baseColumnNames: "module_id", baseTableName: "feed", /* baseTableSchemaName: "core_dev", */constraintName: "FK2FE59EB6140F06", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "module", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-31") {
		addForeignKeyConstraint(baseColumnNames: "feed_id", baseTableName: "feed_file", /* baseTableSchemaName: "core_dev", */constraintName: "FK9DFF9B7D72507A49", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "feed", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-32") {
		addForeignKeyConstraint(baseColumnNames: "stream_id", baseTableName: "feed_file", /* baseTableSchemaName: "core_dev", */constraintName: "FK9DFF9B7D86527F49", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "stream", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-33") {
		addForeignKeyConstraint(baseColumnNames: "feed_id", baseTableName: "feed_user", /* baseTableSchemaName: "core_dev", */constraintName: "FK9E0691CC72507A49", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "feed", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-34") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "feed_user", /* baseTableSchemaName: "core_dev", */constraintName: "FK9E0691CC60701D32", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sec_user", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-35") {
		addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "module", /* baseTableSchemaName: "core_dev", */constraintName: "FKC04BA66C28AB0672", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "module_category", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-36") {
		addForeignKeyConstraint(baseColumnNames: "module_package_id", baseTableName: "module", /* baseTableSchemaName: "core_dev", */constraintName: "FKC04BA66C96E04B35", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "module_package", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-37") {
		addForeignKeyConstraint(baseColumnNames: "module_package_id", baseTableName: "module_category", /* baseTableSchemaName: "core_dev", */constraintName: "FK1AD2C171FEDA9555", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "module_package", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-38") {
		addForeignKeyConstraint(baseColumnNames: "parent_id", baseTableName: "module_category", /* baseTableSchemaName: "core_dev", */constraintName: "FK1AD2C171DFB80526", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "module_category", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-39") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "module_package", /* baseTableSchemaName: "core_dev", */constraintName: "FK8E99557360701D32", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sec_user", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-40") {
		addForeignKeyConstraint(baseColumnNames: "module_package_id", baseTableName: "module_package_user", /* baseTableSchemaName: "core_dev", */constraintName: "FK7EA2BF17FEDA9555", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "module_package", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-41") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "module_package_user", /* baseTableSchemaName: "core_dev", */constraintName: "FK7EA2BF1760701D32", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sec_user", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-42") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "running_signal_path", /* baseTableSchemaName: "core_dev", */constraintName: "FKE44264DC60701D32", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sec_user", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-43") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "saved_signal_path", /* baseTableSchemaName: "core_dev", */constraintName: "FK6A6ED1A460701D32", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sec_user", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-44") {
		addForeignKeyConstraint(baseColumnNames: "sec_role_id", baseTableName: "sec_user_sec_role", /* baseTableSchemaName: "core_dev", */constraintName: "FK6630E2AE201DB64", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sec_role", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-45") {
		addForeignKeyConstraint(baseColumnNames: "sec_user_id", baseTableName: "sec_user_sec_role", /* baseTableSchemaName: "core_dev", */constraintName: "FK6630E2A872C9F44", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sec_user", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-46") {
		addForeignKeyConstraint(baseColumnNames: "feed_id", baseTableName: "stream", /* baseTableSchemaName: "core_dev", */constraintName: "FKCAD54F8072507A49", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "feed", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-47") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "stream", /* baseTableSchemaName: "core_dev", */constraintName: "FKCAD54F8060701D32", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sec_user", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-48") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "task", /* baseTableSchemaName: "core_dev", */constraintName: "FK36358560701D32", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sec_user", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-49") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "tour_user", /* baseTableSchemaName: "core_dev", */constraintName: "FK2ED7F15260701D32", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sec_user", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-50") {
		addForeignKeyConstraint(baseColumnNames: "module_id", baseTableName: "ui_channel", /* baseTableSchemaName: "core_dev", */constraintName: "FK2E3D5E58B6140F06", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "module", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}

	changeSet(author: "admin (generated)", id: "1452621480175-51") {
		addForeignKeyConstraint(baseColumnNames: "running_signal_path_id", baseTableName: "ui_channel", /* baseTableSchemaName: "core_dev", */constraintName: "FK2E3D5E58E9AA551E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "running_signal_path", /* referencedTableSchemaName: "core_dev", */ referencesUniqueColumn: "false")
	}
}
