package core

import com.unifina.utils.IdGenerator
import groovy.json.*
import grails.converters.JSON

databaseChangeLog = {

	changeSet(author: "eric (generated)", id: "1452789453387-1") {
		createTable(tableName: "canvas") {
			column(name: "id", type: "varchar(255)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "canvasPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "adhoc", type: "bit")

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "has_exports", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "json", type: "longtext") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "request_url", type: "varchar(255)")

			column(name: "runner", type: "varchar(255)")

			column(name: "serialization_time", type: "datetime")

			column(name: "serialized", type: "longtext")

			column(name: "server", type: "varchar(255)")

			column(name: "shared", type: "bit")

			column(name: "state", type: "varchar(255)")

			column(name: "type", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "eric (generated)", id: "1452789453387-2") {
		addColumn(tableName: "ui_channel") {
			column(name: "canvas_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "eric (generated)", id: "1452789453387-3") {
		dropForeignKeyConstraint(baseTableName: "running_signal_path", constraintName: "FKE44264DC60701D32")
	}

	changeSet(author: "eric (generated)", id: "1452789453387-4") {
		dropForeignKeyConstraint(baseTableName: "saved_signal_path", constraintName: "FK6A6ED1A460701D32")
	}

	changeSet(author: "eric (generated)", id: "1452789453387-5") {
		dropForeignKeyConstraint(baseTableName: "ui_channel", constraintName: "FK2E3D5E58E9AA551E")
	}

	changeSet(author: "eric (generated)", id: "1452789453387-6") {
		dropIndex(indexName: "runner_idx", tableName: "running_signal_path")
	}

	changeSet(author: "eric (generated)", id: "1452789453387-7") {
		createIndex(indexName: "FKAE7A755860701D32", tableName: "canvas") {
			column(name: "user_id")
		}
	}

	changeSet(author: "eric (generated)", id: "1452789453387-8") {
		createIndex(indexName: "id_uniq_1452789451393", tableName: "canvas", unique: "true") {
			column(name: "id")
		}
	}

	changeSet(author: "eric (generated)", id: "1452789453387-9") {
		createIndex(indexName: "runner_idx", tableName: "canvas") {
			column(name: "runner")
		}
	}

	changeSet(author: "eric (generated)", id: "1452789453387-10") {
		createIndex(indexName: "FK2E3D5E583D649786", tableName: "ui_channel") {
			column(name: "canvas_id")
		}
	}

	changeSet(author: "eric", id: "1452789453387-11") {
		grailsChange {
			change {
				sql.eachRow("SELECT * FROM saved_signal_path") { row ->
					def insertStatement = """
						INSERT INTO canvas (id, version, user_id, date_created, last_updated, name, json, type, has_exports)
						VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
					"""

					// Harmonize JSON
					def oldJson = JSON.parse(row.json)
					def settings = oldJson["signalPathContext"]
					def newJson = oldJson["signalPathData"]
					newJson.settings = settings
					newJson.uiChannel = [:]

					sql.execute(insertStatement,
						IdGenerator.get(),
						0,
						row.user_id,
						row.date_created,
						row.last_updated,
						row.name,
						new JsonBuilder(newJson).toString(),
						row.type,
						row.has_exports
					)
				}



				sql.eachRow("SELECT * FROM running_signal_path") { row ->
					def insertStatement = """
						INSERT INTO canvas (id, version, user_id, date_created, last_updated, name, json, type, has_exports,
							runner, server, request_url, shared, adhoc, state, serialized, serialization_time)
						VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
					"""

					// Harmonize JSON
					def newJson = JSON.parse(row.json)
					newJson.settings = [:]

					def canvasId = IdGenerator.get()

					sql.execute(insertStatement,
						canvasId,
						0,
						row.user_id,
						row.date_created,
						row.last_updated,
						row.name,
						new JsonBuilder(newJson).toString(),
						2,
						false,
						row.runner,
						row.server,
						row.request_url,
						row.shared,
						row.adhoc,
						row.state,
						row.serialized,
						row.serialization_time
					)

					sql.execute("UPDATE ui_channel SET canvas_id = ? WHERE running_signal_path_id = ?", canvasId, row.id)
				}
			}
		}
	}

	changeSet(author: "eric (generated)", id: "1452789453387-12") {
		dropColumn(columnName: "running_signal_path_id", tableName: "ui_channel")
	}

	changeSet(author: "eric (generated)", id: "1452789453387-13") {
		dropTable(tableName: "running_signal_path")
	}

	changeSet(author: "eric (generated)", id: "1452789453387-14") {
		dropTable(tableName: "saved_signal_path")
	}

	changeSet(author: "eric (generated)", id: "1452789453387-15") {
		addForeignKeyConstraint(
			baseColumnNames: "user_id",
			baseTableName: "canvas",
			constraintName: "FKAE7A755860701D32",
			deferrable: "false",
			initiallyDeferred: "false",
			referencedColumnNames: "id",
			referencedTableName: "sec_user",
			referencesUniqueColumn: "false"
		)
	}

	changeSet(author: "eric (generated)", id: "1452789453387-16") {
		addForeignKeyConstraint(
			baseColumnNames: "canvas_id",
			baseTableName: "ui_channel",
			constraintName: "FK2E3D5E583D649786",
			deferrable: "false",
			initiallyDeferred: "false",
			referencedColumnNames: "id",
			referencedTableName: "canvas",
			referencesUniqueColumn: "false"
		)
	}
}
