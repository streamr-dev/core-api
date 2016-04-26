package core

import grails.converters.JSON
import groovy.json.JsonBuilder

databaseChangeLog = {

	changeSet(author: "henripihkala (generated)", id: "1458240759438-1") {
		addColumn(tableName: "dashboard_item") {
			column(name: "canvas_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "henripihkala (generated)", id: "1458240759438-2") {
		addColumn(tableName: "dashboard_item") {
			column(name: "module", type: "int") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "henripihkala (generated)", id: "1458240759438-3") {
		addColumn(tableName: "dashboard_item") {
			column(name: "webcomponent", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "henripihkala", id: "drop-ui-channel-updatedata-1") {
		sql("""
			update dashboard_item
			join ui_channel on dashboard_item.ui_channel_id = ui_channel.id
			set dashboard_item.canvas_id = ui_channel.canvas_id, dashboard_item.module = ui_channel.hash
		""")
	}

	changeSet(author: "henripihkala", id: "drop-ui-channel-updatedata-2") {
		// dig the webcomponents from canvas json
		grailsChange {
			change {
				sql.eachRow("SELECT dashboard_item.id as id, dashboard_item.module as module, canvas.json as json FROM dashboard_item JOIN canvas ON canvas.id = dashboard_item.canvas_id") { row ->
					def updateStatement = """
						UPDATE dashboard_item set webcomponent = ? where dashboard_item.id = ?
					"""

					// Find module json
					def module = JSON.parse(row.json).modules.find {
						it.hash.toString() == row.module.toString()
					}

					if (module) {
						String webcomponent = module.uiChannel.webcomponent
						// Fallback handling for canvases that are so old that they don't have the webcomponent name in the json
						if (!webcomponent) {
							webcomponent = [
									Chart  : "streamr-chart",
									Heatmap: "streamr-heatmap",
									Table  : "streamr-table",
									Label  : "streamr-label"
									// Other webcomponents are too new to have this issue
							].get(module.uiChannel.name)

							if (!webcomponent)
								throw new RuntimeException("Oh no, couldn't find the webcomponent name for dashboard item $row.id! UiChannel name is $module.uiChannel.name")
						}

						sql.executeInsert(updateStatement, webcomponent, row.id)
					}
					else {
						// The dashboard item may point to a non-existing module on the canvas. In that case, print a warning and ignore the row.
						println "Dashboard item $row.id points to a module hash $row.module, but it does not seem to exist!"
					}
				}
			}
		}
	}

	changeSet(author: "henripihkala (generated)", id: "1458240759438-4") {
		dropForeignKeyConstraint(baseTableName: "dashboard_item", constraintName: "FKF4B0C5DE8A8883E5")
	}

	changeSet(author: "henripihkala (generated)", id: "1458240759438-5") {
		dropForeignKeyConstraint(baseTableName: "ui_channel", constraintName: "FK2E3D5E583D649786")
	}

	changeSet(author: "henripihkala (generated)", id: "1458240759438-6") {
		dropForeignKeyConstraint(baseTableName: "ui_channel", constraintName: "FK2E3D5E58B6140F06")
	}

	changeSet(author: "henripihkala (generated)", id: "1458240759438-8") {
		createIndex(indexName: "FKF4B0C5DE3D649786", tableName: "dashboard_item") {
			column(name: "canvas_id")
		}
	}

	changeSet(author: "henripihkala (generated)", id: "1458240759438-9") {
		dropColumn(columnName: "ui_channel_id", tableName: "dashboard_item")
	}

	changeSet(author: "henripihkala (generated)", id: "1458240759438-10") {
		dropTable(tableName: "ui_channel")
	}

	changeSet(author: "henripihkala (generated)", id: "1458240759438-7") {
		addForeignKeyConstraint(baseColumnNames: "canvas_id", baseTableName: "dashboard_item", constraintName: "FKF4B0C5DE3D649786", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "canvas", referencesUniqueColumn: "false")
	}
}
