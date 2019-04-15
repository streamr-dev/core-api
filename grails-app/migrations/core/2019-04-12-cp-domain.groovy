package core
databaseChangeLog = {
	changeSet(author: "kkn", id: "cp-domain-1") {
		createTable(tableName: "community_join_request") {
			column(name: "id", type: "varchar(255)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "community_joiPK")
			}
			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}
			column(name: "community_address", type: "varchar(255)") {
				constraints(nullable: "false")
			}
			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}
			column(name: "last_updated", type: "datetime") {
				constraints(nullable: "false")
			}
			column(name: "member_address", type: "varchar(255)") {
				constraints(nullable: "false")
			}
			column(name: "state", type: "integer") {
				constraints(nullable: "false")
			}
			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}
	changeSet(author: "kkn", id: "cp-domain-2") {
		createTable(tableName: "community_secret") {
			column(name: "id", type: "varchar(255)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "community_secPK")
			}
			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}
			column(name: "community_address", type: "varchar(255)") {
				constraints(nullable: "false")
			}
			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}
			column(name: "secret", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}
	changeSet(author: "kkn", id: "cp-domain-3") {
		addColumn(tableName: "product") {
			column(name: "type", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}
	changeSet(author: "kkn", id: "cp-domain-5") {
		createIndex(indexName: "user_idx", tableName: "community_join_request") {
			column(name: "user_id")
		}
	}
	changeSet(author: "kkn", id: "cp-domain-6") {
		createIndex(indexName: "state_idx", tableName: "community_join_request") {
			column(name: "state")
		}
	}
	changeSet(author: "kkn", id: "cp-domain-7") {
		createIndex(indexName: "type_idx", tableName: "product") {
			column(name: "type")
		}
	}
	changeSet(author: "kkn", id: "cp-domain-4") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "community_join_request", constraintName: "joinreq_user_idx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "sec_user", referencesUniqueColumn: "false")
	}
}
