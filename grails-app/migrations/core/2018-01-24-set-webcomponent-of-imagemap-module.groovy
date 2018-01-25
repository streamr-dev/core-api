package core

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

databaseChangeLog = {
	changeSet(author: "aapeli", id: "set-webcomponent-of-imagemap-module") {
		update(tableName: "module") {
			column(name: "webcomponent", value: "streamr-map")
			where("id = 583")
		}
	}
}
