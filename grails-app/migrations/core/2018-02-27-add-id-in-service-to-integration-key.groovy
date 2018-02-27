package core

import groovy.json.JsonSlurper

databaseChangeLog = {

	changeSet(author: "eric", id: "add-id-in-service-to-integration-key-1") {
		addColumn(tableName: "integration_key") {
			column(name: "id_in_service", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "eric", id: "add-id-in-service-to-integration-key-2") {
		grailsChange {
			change {
				sql.eachRow('SELECT id, json FROM integration_key WHERE service = "ETHEREUM" OR service = "ETHEREUM_ID"') { row ->
					String keyId = row['id']
					String json = row['json']

					Map<String, String> jsonMap = new JsonSlurper().parseText(json)
					sql.execute('UPDATE integration_key SET id_in_service = ? WHERE id = ?', jsonMap.address, keyId)
				}
			}
		}
	}

	changeSet(author: "eric", id: "add-id-in-service-to-integration-key-3") {
		createIndex(indexName: "id_in_service_and_service_idx", tableName: "integration_key") {
			column(name: "id_in_service")
			column(name: "service")
		}
	}
}
