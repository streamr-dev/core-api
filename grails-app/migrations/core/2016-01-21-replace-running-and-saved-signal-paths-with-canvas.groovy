package core

import groovy.json.*
import grails.converters.JSON
import org.apache.commons.codec.binary.Base64

import java.nio.ByteBuffer


databaseChangeLog = {

	changeSet(author: "eric", id: "1453384829304-1") {
		createTable(tableName: "canvas") {
			column(name: "id", type: "varchar(255)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "canvasPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "adhoc", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "example", type: "bit") {
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

			column(name: "shared", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "state", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "eric", id: "1453384829304-2") {
		addColumn(tableName: "ui_channel") {
			column(name: "canvas_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "eric", id: "1453384829304-3") {
		dropForeignKeyConstraint(baseTableName: "running_signal_path", constraintName: "FKE44264DC60701D32")
	}

	changeSet(author: "eric", id: "1453384829304-4") {
		dropForeignKeyConstraint(baseTableName: "saved_signal_path", constraintName: "FK6A6ED1A460701D32")
	}

	changeSet(author: "eric", id: "1453384829304-5") {
		dropForeignKeyConstraint(baseTableName: "ui_channel", constraintName: "FK2E3D5E58E9AA551E")
	}

	changeSet(author: "eric", id: "1453384829304-6") {
		dropIndex(indexName: "runner_idx", tableName: "running_signal_path")
	}

	changeSet(author: "eric", id: "1453384829304-7") {
		createIndex(indexName: "FKAE7A755860701D32", tableName: "canvas") {
			column(name: "user_id")
		}
	}

	changeSet(author: "eric", id: "1453384829304-8") {
		createIndex(indexName: "runner_idx", tableName: "canvas") {
			column(name: "runner")
		}
	}

	changeSet(author: "eric", id: "1453384829304-9") {
		createIndex(indexName: "FK2E3D5E583D649786", tableName: "ui_channel") {
			column(name: "canvas_id")
		}
	}

	changeSet(author: "eric", id: "1453384829304-10") {
		grailsChange {
			change {
				sql.eachRow("SELECT * FROM saved_signal_path") { row ->
					def insertStatement = """
						INSERT INTO canvas (id, version, user_id, date_created, last_updated, name, json, state, has_exports, example, adhoc, shared)
						VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
						row.type == 1,
						false,
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
						row.shared == null ? false : row.shared,
						row.adhoc,
						row.serialized,
						row.serialization_time
					)

					sql.execute("UPDATE ui_channel SET canvas_id = ? WHERE running_signal_path_id = ?", canvasId, row.id)
				}
			}
		}
	}

	changeSet(author: "eric", id: "1453384829304-11") {
		dropColumn(columnName: "running_signal_path_id", tableName: "ui_channel")
	}

	changeSet(author: "eric", id: "1453384829304-12") {
		dropTable(tableName: "running_signal_path")
	}

	changeSet(author: "eric", id: "1453384829304-13") {
		dropTable(tableName: "saved_signal_path")
	}

	changeSet(author: "eric", id: "1453384829304-14") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "canvas", constraintName: "FKAE7A755860701D32", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "sec_user", referencesUniqueColumn: "false")
	}

	changeSet(author: "eric", id: "1453384829304-15") {
		addForeignKeyConstraint(baseColumnNames: "canvas_id", baseTableName: "ui_channel", constraintName: "FK2E3D5E583D649786", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "canvas", referencesUniqueColumn: "false")
	}

	changeSet(author: "eric", id: "14533384829304-16", context: "test") {
		grailsChange {
			change {
				sql.executeInsert("INSERT INTO canvas (id, version, adhoc, date_created, example, has_exports, json," +
					"last_updated, name, request_url, runner, serialization_time, serialized, server, shared, state," +
					"user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
					'jklads9812jlsdf09dfgjoaq',
					3,
					false,
					'2015-11-15 18:09:59',
					false,
					false,
					'{"settings": {}, "modules": [], "name": "BrokenSerialization"}',
					'2016-02-01 15:29:45',
					'BrokenSerialization',
					'http://192.168.11.42:8081/unifina-core/api/live/request',
					's-1454340584152',
					'2016-02-01 15:37:30',
					'{\"serialization\": \"is broken\"}',
					'192.168.11.42',
					false,
					'stopped',
					1
				)

				sql.executeInsert("INSERT INTO canvas (id, version, adhoc, date_created, example, has_exports, json," +
					"last_updated, name, request_url, runner, serialization_time, serialized, server, shared, state," +
					"user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
					'kldfaj2309jr9wjf9ashjg9sdgu9',
					3,
					false,
					'2015-11-15 18:09:59',
					false,
					false,
					'{"settings": {}, "modules": [], "name": "StopCanvasApiSpec"}',
					'2016-02-01 15:29:45',
					'StopCanvasApiSpec',
					'http://192.168.10.21:8081/unifina-core/api/live/request',
					's-1454340584152',
					null,
					"",
					'192.168.10.21',
					false,
					'running',
					1
				)
			}
		}
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
