package core

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

databaseChangeLog = {
	changeSet(author: "aapeli", id: "set-webcomponent-of-existing-canvas-modules") {
		grailsChange {
			change {
				def webcomponentNameByModuleName = [
						67: 'streamr-chart',
						214: 'streamr-map',
						583: 'streamr-map',
						196: 'streamr-heatmap',
						145: 'streamr-label',
						527: 'streamr-table',
						218: 'streamr-button',
						219: 'streamr-switcher',
						220: 'streamr-text-field',
						236: 'streamr-table',
						1011: 'streamr-table',
				]

				sql.eachRow('SELECT id, json FROM canvas') { row ->
					String canvasId = row['id']
					String json = row['json']

					Map<String, String> jsonMap = new JsonSlurper().parseText(json)
					jsonMap.modules.each {
						if (it.uiChannel) {
							def webcomponent = webcomponentNameByModuleName[it.id]
							if (webcomponent) {
								it.uiChannel.webcomponent = webcomponent
							}
						}
					}
					def newJson = new JsonBuilder(jsonMap).toString()

					sql.execute('UPDATE canvas SET json = ? WHERE id = ?', newJson, canvasId)
				}
			}
		}
	}
}
