package core

databaseChangeLog = {

	changeSet(author: "eric", id: "separate-serialization-domain-class-1") {
		createTable(tableName: "serialization") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "serializationPK")
			}
			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}
			column(name: "bytes", type: "longblob") {
				constraints(nullable: "false")
			}
			column(name: "date", type: "datetime") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "eric", id: "separate-serialization-domain-class-2") {
		addColumn(tableName: "canvas") {
			column(name: "serialization_id", type: "bigint") {
				constraints(unique: "true")
			}
		}
	}

	changeSet(author: "eric", id: "separate-serialization-domain-class-3") {
		grailsChange {
			change {
				sql.eachRow("SELECT id, serialization_time, serialized FROM canvas WHERE serialization_time IS NOT NULL") { row ->
					def res = sql.executeInsert("INSERT INTO serialization (version, date, bytes) VALUES (0, ?, ?)",
						row["serialization_time"],
						row["serialized"]
					)
					def canvasId = row["id"]
					def serializationId = res[0][0]
					sql.execute("UPDATE canvas SET serialization_id = ? WHERE id = ?", serializationId, canvasId)
				}
			}
		}
	}

	changeSet(author: "eric", id: "separate-serialization-domain-class-4") {
		createIndex(indexName: "FKAE7A755835F2A96E", tableName: "canvas") {
			column(name: "serialization_id")
		}
	}

	changeSet(author: "eric", id: "separate-serialization-domain-class-5") {
		createIndex(indexName: "serialization_id_uniq_1484920841951", tableName: "canvas", unique: "true") {
			column(name: "serialization_id")
		}
	}

	changeSet(author: "eric", id: "separate-serialization-domain-class-6") {
		dropColumn(columnName: "serialization_time", tableName: "canvas")
	}

	changeSet(author: "eric", id: "separate-serialization-domain-class-7") {
		dropColumn(columnName: "serialized", tableName: "canvas")
	}

	changeSet(author: "eric", id: "separate-serialization-domain-class-8") {
		addForeignKeyConstraint(
			baseColumnNames: "serialization_id",
			baseTableName: "canvas",
			constraintName: "FKAE7A755835F2A96E",
			deferrable: "false",
			initiallyDeferred: "false",
			referencedColumnNames: "id",
			referencedTableName: "serialization",
			referencesUniqueColumn: "false"
		)
	}
}
