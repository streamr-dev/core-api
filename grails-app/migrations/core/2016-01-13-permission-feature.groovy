package core

databaseChangeLog = {

	changeSet(author: "jtakalai (generated)", id: "1452674923112-1") {
		createTable(tableName: "permission") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "permissionPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "clazz", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "long_id", type: "bigint") {
				constraints(nullable: "true")
			}

			column(name: "operation", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "string_id", type: "varchar(255)") {
				constraints(nullable: "true")
			}

			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	// migrate data
	// old system was ModulePackageUser and FeedUser connecting users (beyond modulePackage.user) that can access the resource
	// new system is Permission table that combines all those
	changeSet(author: "jtakalai (generated)", id: "1452674923112-4") {
		sql("INSERT INTO permission (id, version, clazz, long_id, operation, string_id, user_id) " +
				"SELECT NULL, 0, 'com.unifina.domain.signalpath.ModulePackage', module_package_id, 'read', NULL, user_id FROM module_package_user")
		sql("INSERT INTO permission (id, version, clazz, long_id, operation, string_id, user_id) " +
				"SELECT NULL, 0, 'com.unifina.domain.data.Feed', feed_id, 'read', NULL, user_id FROM feed_user")
	}

	changeSet(author: "jtakalai (generated)", id: "1452674923112-3") {
		createIndex(indexName: "FKE125C5CF60701D32", tableName: "permission") {
			column(name: "user_id")
		}
	}

	changeSet(author: "jtakalai (generated)", id: "1452674923112-2") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "permission", constraintName: "FKE125C5CF60701D32", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "sec_user", referencesUniqueColumn: "false")
	}
}
