package core

import grails.converters.JSON
import groovy.json.JsonBuilder
import org.apache.log4j.Logger

final Logger logger = Logger.getLogger("change-canvas-stream-modules-id")

databaseChangeLog = {

	changeSet(author: "eric", id: "change-canvas-stream-modules-id-1") {
		grailsChange {
			change {
				sql.eachRow("SELECT id, json FROM canvas") { row ->
					Map signalPathMap = JSON.parse(row.json)

					signalPathMap["modules"].each {
						def module = it
						if (module.name == "Stream") {
							assert module.params.size() == 1

							String identifier = module.params[0].value

							def result
							try {
								Long longIdentifier = Long.parseLong(identifier)
								result = sql.firstRow("SELECT uuid FROM stream WHERE id = ?", longIdentifier)
							} catch (NumberFormatException e) {
								result = sql.firstRow("SELECT uuid FROM stream WHERE name = ?", identifier)
							}

							if (result?.uuid) {
								//logger.info("Mapping ${identifier} -> ${result.uuid} (Canvas ${row['id']}, hash ${module.hash})")
								module.params[0].value = result.uuid
							} else {
								logger.warn("Mapping for ${identifier} not found! (Canvas ${row['id']}, hash ${module.hash})")
							}
						}
					}

					def newJson = new JsonBuilder(signalPathMap).toString()

					sql.execute("UPDATE canvas SET json = ? WHERE id = ?", newJson, row["id"])
				}
			}
		}
	}

	changeSet(author: "eric", id: "change-canvas-stream-modules-id-2") {
		addColumn(tableName: "feed_file") {
			column(name: "stream_uuid", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "eric", id: "change-canvas-stream-modules-id-3") {
		grailsChange {
			change {
				def idToUuid = [:]
				sql.eachRow("SELECT id, uuid FROM stream") { idToUuid[it.id] = it.uuid }

				sql.eachRow("SELECT id, stream_id FROM feed_file") { row ->
					def id = row["id"]
					def uuid = idToUuid[row["stream_id"]]
					sql.execute("UPDATE feed_file SET stream_uuid = ? WHERE id = ?", uuid, id)
				}
			}
		}
	}

	changeSet(author: "eric", id: "change-canvas-stream-modules-id-4") {
		dropForeignKeyConstraint(baseTableName: "feed_file", constraintName: "FK9DFF9B7D86527F49")
		dropColumn(columnName: "stream_id", tableName: "feed_file")
	}

	changeSet(author: "eric", id: "change-canvas-stream-modules-id-5") {
		dropColumn(columnName: "id", tableName: "stream")
	}

	changeSet(author: "eric", id: "change-canvas-stream-modules-id-6") {
		renameColumn(
				tableName: "stream",
				oldColumnName: "uuid",
				newColumnName: "id",
				columnDataType: "varchar(255)"
		)
		addPrimaryKey(
				tableName: "stream",
				constraintName: "pk_stream_id",
				columnNames: "id",
		)
	}

	changeSet(author: "eric", id: "change-canvas-stream-modules-id-7") {
		renameColumn(
				tableName: "feed_file",
				oldColumnName: "stream_uuid",
				newColumnName: "stream_id",
				columnDataType: "varchar(255)",
		)
	}

	changeSet(author: "eric", id: "change-canvas-stream-modules-id-8") {
		addNotNullConstraint(columnDataType: "varchar(255)", columnName: "stream_id", tableName: "feed_file")
	}

	changeSet(author: "eric", id: "change-canvas-stream-modules-id-9") {
		createIndex(indexName: "stream_idx", tableName: "feed_file") {
			column(name: "stream_id")
		}
	}

	changeSet(author: "eric", id: "change-canvas-stream-modules-id-10") {
		addForeignKeyConstraint(
				baseColumnNames: "stream_id",
				baseTableName: "feed_file",
				constraintName: "FK9DFF7A2F49034A50",
				deferrable: "false",
				initiallyDeferred: "false",
				referencedColumnNames: "id",
				referencedTableName: "stream",
				referencesUniqueColumn: "false",
		)
	}
}
