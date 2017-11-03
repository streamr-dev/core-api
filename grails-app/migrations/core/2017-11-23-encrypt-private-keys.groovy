package core

import com.unifina.security.StringEncryptor
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

databaseChangeLog = {
	changeSet(author: "eric", id: "encrypt-private-keys") {
		grailsChange {
			change {
				def encryptor = new StringEncryptor(System.getProperty("streamr.encryption.password"))

				sql.eachRow('SELECT id, user_id, json FROM integration_key WHERE service = "ETHEREUM"') { row ->
					String keyId = row['id']
					Long userId = row['user_id']
					String json = row['json']

					Map<String, String> jsonMap = new JsonSlurper().parseText(json)
					def encryptedPrivateKey = encryptor.encrypt(jsonMap.privateKey, userId.byteValue())
					jsonMap.privateKey = encryptedPrivateKey
					def newJson = new JsonBuilder(jsonMap).toString()

					sql.execute('UPDATE integration_key SET json = ? WHERE id = ?', newJson, keyId)
				}
			}
		}
	}
}
