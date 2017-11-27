package core

import com.google.gson.Gson
import com.unifina.utils.IdGenerator
import grails.converters.JSON
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.apache.log4j.Logger

final Logger logger = Logger.getLogger("migrate-eth-accounts-as-integration-keys")

databaseChangeLog = {

	changeSet(author: "eric", id: "migrate-eth-accounts-as-integration-keys") {
		grailsChange {
			change {
				def keyAndUserToId = [:]
				def userToNumber = [:]

				sql.eachRow("SELECT id, json, user_id FROM canvas") { row ->
					String canvasId = row['id']
					String json = row['json']
					Integer userId = row['user_id']


					Map canvas = new JsonSlurper().parseText(json)

					canvas.modules.each { module ->
						if (module.id == 1020 || module.id == 1021) { // EthereumCall and SolidityModule
							if (module.options.privateKey) {
								def address = module.options.address.value
								def privateKey = module.options.privateKey.value

								String mapKey = userId + "-" + privateKey
								if (!keyAndUserToId[mapKey]) {

									if (!userToNumber.containsKey(userId)) {
										userToNumber[userId] = 0
									}
									userToNumber[userId] += 1

									keyAndUserToId[mapKey] = IdGenerator.get()
									sql.execute('INSERT INTO integration_key (id, version, name, date_created, last_updated, json, user_id, service) VALUES (?, 0, ?, NOW(), NOW(), ?, ?, ?)',
										keyAndUserToId[mapKey],
										"Migrated key #" + userToNumber[userId],
										"{\"privateKey\":\"" + privateKey + "\",\"address\":\"" + address + "\"}",
										userId,
										"ETHEREUM")
								}

								module.params.add([ name: "ethAccount", value: keyAndUserToId[mapKey]])
							}
						}
					}

					def newJson = new JsonBuilder(canvas).toString()
					sql.execute("UPDATE canvas SET json = ? WHERE id = ?", newJson, canvasId)
				}
			}
		}
	}
}
