package core

import org.apache.log4j.Logger

final Logger logger = Logger.getLogger("domain-fields-for-permissions")

databaseChangeLog = {

	changeSet(author: "eric", id: "domain-fields-for-permissions-1") {
		addColumn(tableName: "permission") {
			column(name: "canvas_id", type: "varchar(255)")
		}
	}

	changeSet(author: "eric", id: "domain-fields-for-permissions-2") {
		addColumn(tableName: "permission") {
			column(name: "dashboard_id", type: "varchar(255)")
		}
	}

	changeSet(author: "eric", id: "domain-fields-for-permissions-3") {
		addColumn(tableName: "permission") {
			column(name: "feed_id", type: "bigint")
		}
	}

	changeSet(author: "eric", id: "domain-fields-for-permissions-4") {
		addColumn(tableName: "permission") {
			column(name: "module_package_id", type: "bigint")
		}
	}

	changeSet(author: "eric", id: "domain-fields-for-permissions-5") {
		addColumn(tableName: "permission") {
			column(name: "stream_id", type: "varchar(255)")
		}
	}

	changeSet(author: "eric", id: "domain-fields-for-permissions-6") {
		createIndex(indexName: "FKE125C5CF3D649786", tableName: "permission") {
			column(name: "canvas_id")
		}
	}

	changeSet(author: "eric", id: "domain-fields-for-permissions-7") {
		createIndex(indexName: "FKE125C5CF70E281EB", tableName: "permission") {
			column(name: "dashboard_id")
		}
	}

	changeSet(author: "eric", id: "domain-fields-for-permissions-8") {
		createIndex(indexName: "FKE125C5CF72507A49", tableName: "permission") {
			column(name: "feed_id")
		}
	}

	changeSet(author: "eric", id: "domain-fields-for-permissions-9") {
		createIndex(indexName: "FKE125C5CF86527F49", tableName: "permission") {
			column(name: "stream_id")
		}
	}

	changeSet(author: "eric", id: "domain-fields-for-permissions-10") {
		createIndex(indexName: "FKE125C5CFFEDA9555", tableName: "permission") {
			column(name: "module_package_id")
		}
	}

	changeSet(author: "eric", id: "domain-fields-for-permissions-11") {
		addForeignKeyConstraint(
			baseColumnNames: "canvas_id",
			baseTableName: "permission",
			constraintName: "FKE125C5CF3D649786",
			deferrable: "false",
			initiallyDeferred: "false",
			referencedColumnNames: "id",
			referencedTableName: "canvas",
			referencesUniqueColumn: "false"
		)
	}

	changeSet(author: "eric", id: "domain-fields-for-permissions-12") {
		addForeignKeyConstraint(
			baseColumnNames: "dashboard_id",
			baseTableName: "permission",
			constraintName: "FKE125C5CF70E281EB",
			deferrable: "false",
			initiallyDeferred: "false",
			referencedColumnNames: "id",
			referencedTableName: "dashboard",
			referencesUniqueColumn: "false"
		)
	}

	changeSet(author: "eric", id: "domain-fields-for-permissions-13") {
		addForeignKeyConstraint(
			baseColumnNames: "feed_id",
			baseTableName: "permission",
			constraintName: "FKE125C5CF72507A49",
			deferrable: "false",
			initiallyDeferred: "false",
			referencedColumnNames: "id",
			referencedTableName: "feed",
			referencesUniqueColumn: "false"
		)
	}

	changeSet(author: "eric", id: "domain-fields-for-permissions-14") {
		addForeignKeyConstraint(
			baseColumnNames: "module_package_id",
			baseTableName: "permission",
			constraintName: "FKE125C5CFFEDA9555",
			deferrable: "false",
			initiallyDeferred: "false",
			referencedColumnNames: "id",
			referencedTableName: "module_package",
			referencesUniqueColumn: "false"
		)
	}

	changeSet(author: "eric", id: "domain-fields-for-permissions-15") {
		addForeignKeyConstraint(
			baseColumnNames: "stream_id",
			baseTableName: "permission",
			constraintName: "FKE125C5CF86527F49",
			deferrable: "false",
			initiallyDeferred: "false",
			referencedColumnNames: "id",
			referencedTableName: "stream",
			referencesUniqueColumn: "false"
		)
	}

	changeSet(author: "eric", id: "domain-fields-for-permissions-16") {
		grailsChange {
			change {

				def updateValues = []

				sql.eachRow("SELECT id, clazz, long_id, string_id FROM permission") { row ->
					Long id = row['id']
					String clazz = row['clazz']
					Long longId = row['long_id']
					String stringId = row['string_id']

					if (clazz == 'com.unifina.domain.signalpath.Canvas') {
						updateValues.push([id, "canvas_id", stringId])
					} else if (clazz == 'com.unifina.domain.dashboard.Dashboard') {
						updateValues.push([id, "dashboard_id", stringId])
					} else if (clazz == 'com.unifina.domain.data.Feed') {
						updateValues.push([id, "feed_id", longId])
					} else if (clazz == 'com.unifina.domain.signalpath.ModulePackage') {
						updateValues.push([id, "module_package_id", longId])
					} else if (clazz == 'com.unifina.domain.data.Stream') {
						updateValues.push([id, "stream_id", stringId])
					} else {
						throw new RuntimeException("Unexpected clazz in Permissions table: " + clazz)
					}
				}

				updateValues.each { values ->
					Long id = values[0]
					String field = values[1]
					Object foreignId = values[2]
					//logger.info("UPDATE permission SET ${field} = '${foreignId}' WHERE id = '${id}'")
					sql.execute("UPDATE permission SET ${field} = ? WHERE id = ?", foreignId, id)
				}
			}
		}
	}


	changeSet(author: "eric", id: "domain-fields-for-permissions-17") {
		dropColumn(columnName: "clazz", tableName: "permission")
	}

	changeSet(author: "eric", id: "domain-fields-for-permissions-18") {
		dropColumn(columnName: "long_id", tableName: "permission")
	}

	changeSet(author: "eric", id: "domain-fields-for-permissions-19") {
		dropColumn(columnName: "string_id", tableName: "permission")
	}
}
