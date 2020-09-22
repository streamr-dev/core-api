package core

import com.unifina.domain.IntegrationKey
import com.unifina.domain.SignupMethod
import com.unifina.domain.Stream
import com.unifina.domain.ExampleType
import com.unifina.domain.Permission
import com.unifina.utils.IdGenerator
import com.unifina.security.ApiKeyConverter
import com.unifina.service.EthereumIntegrationKeyService
import com.unifina.utils.AlphanumericStringGenerator
import grails.converters.JSON
import groovy.sql.Sql

def getAccountAddress(String apiKeyId) {
	String privateKey = ApiKeyConverter.createEthereumPrivateKey(apiKeyId)
	return "0x" + EthereumIntegrationKeyService.getAddress(privateKey)
}

def createIntegrationKey(String integrationKeyName, String accountAddress, Integer userId, Sql sql) {
	Date now = new Date()
	return sql.executeInsert("INSERT INTO integration_key (id, version, name, json, user_id, service, id_in_service, date_created, last_updated) VALUE (?, ?, ?, ?, ?, ?, ?, ?, ?)", [
		IdGenerator.get(),
		0,
		integrationKeyName,
		([ address: accountAddress ] as JSON).toString(),
		userId,
		IntegrationKey.Service.ETHEREUM_ID.name(),
		accountAddress,
		now,
		now
	]);
}

def createInboxStream(long userId, String accountAddress, Sql sql) {
	Date now = new Date();
	sql.executeInsert('INSERT INTO stream (version, id, name, date_created, last_updated, partitions, require_signed_data, auto_configure, storage_days, inbox, inactivity_threshold_hours, example_type, require_encrypted_data) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)', [
		0,
		accountAddress,
		accountAddress,
		now,
		now,
		1,
		false,
		false,
		Stream.DEFAULT_STORAGE_DAYS,
		true,
		Stream.DEFAULT_INACTIVITY_THRESHOLD_HOURS,
		ExampleType.NOT_SET.value,
		false
	]);
	List<Permission.Operation> permissionOperations = [
		Permission.Operation.STREAM_GET,
		Permission.Operation.STREAM_EDIT,
		Permission.Operation.STREAM_DELETE,
		Permission.Operation.STREAM_PUBLISH,
		Permission.Operation.STREAM_SUBSCRIBE,
		Permission.Operation.STREAM_SHARE,
	];
	permissionOperations.each { op ->
		sql.executeInsert('INSERT INTO permission (version, operation, stream_id, anonymous, user_id) VALUES (?, ?, ?, ?, ?)', [
			0,
			op.id,
			accountAddress,
			false,
			userId
		]);
	}
}

databaseChangeLog = {

	// For each API key that is attached to a user:
	// - create a new IntegrationKey for the user
	// - delete the permissions attached to the key
	// - delete the key
	changeSet(author: "teogeb", id: "convert-API-keys-to-Ethereum-IDs-1") {
		grailsChange {
			change {
				sql.eachRow('SELECT id, name, user_id FROM `key` where user_id is not null') { row ->
					String apiKeyId = row['id']
					String apiKeyName = row['name']
					int userId = row['user_id']
					String accountAddress = getAccountAddress(apiKeyId)
					String integrationKeyName = 'Converted from API key: ' + apiKeyName
					createIntegrationKey(integrationKeyName, accountAddress, userId, sql)
					sql.execute("DELETE FROM permission WHERE key_id = ?", [apiKeyId])
				}
			}
		}
		sql('DELETE FROM `key` where user_id is not null')
	}

	// For each anonymous API key:
	// - create a new Ethereum user (and inbox stream for that user)
	// - migrate the key’s permissions to that user
	// - delete the key
	changeSet(author: "teogeb", id: "convert-API-keys-to-Ethereum-IDs-2") {
		grailsChange {
			change {
				sql.eachRow('SELECT id, name, user_id FROM `key` where user_id is null') { row ->
					String apiKeyId = row['id']
					String accountAddress = getAccountAddress(apiKeyId)
					def insertResult = sql.executeInsert("INSERT INTO user (version, account_expired, account_locked, enabled, name, password, password_expired, username, signup_method) VALUE (?, ?, ?, ?, ?, ?, ?, ?, ?)", [
						0,
						false,
						false,
						true,
						"Anonymous User",
						AlphanumericStringGenerator.getRandomAlphanumericString(32),
						false,
						accountAddress,
						SignupMethod.UNKNOWN.name()
					]);
					int userId = insertResult[0][0]
					sql.execute("UPDATE permission SET user_id = ?, key_id = null WHERE key_id = ?", [ userId, apiKeyId])
					createIntegrationKey(accountAddress, accountAddress, userId, sql)
					createInboxStream(userId, accountAddress, sql)
				}
			}
		}
		sql('DELETE FROM `key` where user_id is null')
	}
}
