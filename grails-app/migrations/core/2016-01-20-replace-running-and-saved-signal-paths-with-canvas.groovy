package core

import groovy.json.*
import grails.converters.JSON
import org.apache.commons.codec.binary.Base64

import java.nio.ByteBuffer

databaseChangeLog = {

	changeSet(author: "harbu1 (generated)", id: "1453299053074-1") {
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

			column(name: "example", type: "bit") {
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

			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "harbu1 (generated)", id: "1453299053074-2") {
		addColumn(tableName: "ui_channel") {
			column(name: "canvas_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "harbu1 (generated)", id: "1453299053074-4") {
		dropForeignKeyConstraint(baseTableName: "running_signal_path", constraintName: "FKE44264DC60701D32")
	}

	changeSet(author: "harbu1 (generated)", id: "1453299053074-5") {
		dropForeignKeyConstraint(baseTableName: "saved_signal_path", constraintName: "FK6A6ED1A460701D32")
	}

	changeSet(author: "harbu1 (generated)", id: "1453299053074-6") {
		dropForeignKeyConstraint(baseTableName: "ui_channel", constraintName: "FK2E3D5E58E9AA551E")
	}

	changeSet(author: "harbu1 (generated)", id: "1453299053074-9") {
		dropIndex(indexName: "runner_idx", tableName: "running_signal_path")
	}

	changeSet(author: "harbu1 (generated)", id: "1453299053074-10") {
		createIndex(indexName: "FKAE7A755860701D32", tableName: "canvas") {
			column(name: "user_id")
		}
	}

	changeSet(author: "harbu1 (generated)", id: "1453299053074-11") {
		createIndex(indexName: "runner_idx", tableName: "canvas") {
			column(name: "runner")
		}
	}

	changeSet(author: "harbu1 (generated)", id: "1453299053074-12") {
		createIndex(indexName: "FK2E3D5E583D649786", tableName: "ui_channel") {
			column(name: "canvas_id")
		}
	}

	changeSet(author: "eric", id: "custom-sql-inserts") {
		grailsChange {
			change {
				sql.eachRow("SELECT * FROM saved_signal_path") { row ->
					def insertStatement = """
						INSERT INTO canvas (id, version, user_id, date_created, last_updated, name, json, state, has_exports, example)
						VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
					"""

					// Harmonize JSON
					def oldJson = JSON.parse(row.json)
					def settings = oldJson["signalPathContext"]
					def newJson = oldJson["signalPathData"]
					newJson.settings = settings
					newJson.uiChannel = [:]

					sql.executeInsert(insertStatement,
						generateId(),
						0,
						row.user_id,
						row.date_created,
						row.last_updated,
						row.name,
						new JsonBuilder(newJson).toString(),
						"stopped",
						row.has_exports == true,
						row.type == 1
					)
				}



				sql.eachRow("SELECT * FROM running_signal_path") { row ->
					def insertStatement = """
						INSERT INTO canvas (id, version, user_id, date_created, last_updated, name, json, state, has_exports, example, runner, server, request_url, shared, adhoc, serialized, serialization_time)
						VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
					"""

					// Harmonize JSON
					def newJson = JSON.parse(row.json)
					newJson.settings = [:]

					def canvasId = generateId()

					sql.executeInsert(insertStatement,
						canvasId,
						0,
						row.user_id,
						row.date_created,
						row.last_updated,
						row.name,
						new JsonBuilder(newJson).toString(),
						row.state == "running" ? "running" : "stopped",
						false,
						false,
						row.runner,
						row.server,
						row.request_url,
						row.shared,
						row.adhoc,
						row.serialized,
						row.serialization_time
					)

					sql.execute("UPDATE ui_channel SET canvas_id = ? WHERE running_signal_path_id = ?", canvasId, row.id)
				}
			}
		}
	}


	changeSet(author: "harbu1 (generated)", id: "1453299053074-13") {
		dropColumn(columnName: "running_signal_path_id", tableName: "ui_channel")
	}

	changeSet(author: "harbu1 (generated)", id: "1453299053074-15") {
		dropTable(tableName: "running_signal_path")
	}

	changeSet(author: "harbu1 (generated)", id: "1453299053074-16") {
		dropTable(tableName: "saved_signal_path")
	}

	changeSet(author: "harbu1 (generated)", id: "1453299053074-7") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "canvas", constraintName: "FKAE7A755860701D32", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "sec_user", referencesUniqueColumn: "false")
	}

	changeSet(author: "harbu1 (generated)", id: "1453299053074-8") {
		addForeignKeyConstraint(baseColumnNames: "canvas_id", baseTableName: "ui_channel", constraintName: "FK2E3D5E583D649786", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "canvas", referencesUniqueColumn: "false")
	}
}

def generateId() {
	UUID uuid = UUID.randomUUID();

	byte[] bytes = new byte[16];
	ByteBuffer bb = ByteBuffer.wrap(bytes);
	bb.putLong(uuid.getMostSignificantBits());
	bb.putLong(uuid.getLeastSignificantBits());

	return Base64.encodeBase64URLSafeString(bytes);
}