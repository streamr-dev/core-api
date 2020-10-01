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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.lang.Runtime

class GetAccountAddressTask implements Callable<String> {
	private String apiKey

	GetAccountAddressTask(String apiKey) {
		this.apiKey = apiKey
	}

	@Override
	String call() throws Exception {
		String privateKey = ApiKeyConverter.createEthereumPrivateKey(apiKey)
		return "0x" + EthereumIntegrationKeyService.getAddress(privateKey)
	}
}

def convertUserKeysToIntegrationKeys(Sql sql) {
	String statement = """
		INSERT INTO
			integration_key (id, version, name, json, user_id, service, id_in_service, date_created, last_updated)
		SELECT
			generated_integration_key_id,
			0,
			CONCAT('Converted from API key: ',key_id),
			CONCAT(CONCAT('{\"address\":\"',account_address),'\"}'),
			user_id,
			?,
			account_address,
			CURRENT_TIMESTAMP(),
			CURRENT_TIMESTAMP()
		FROM `key`
		LEFT JOIN tmp_apikey_lookup ON tmp_apikey_lookup.key_id = `key`.id
		WHERE `key`.user_id is not null
	"""
	sql.execute(statement, [IntegrationKey.Service.ETHEREUM_ID.name()])
}

databaseChangeLog = {

	// fill a lookup table
	changeSet(author: "teogeb", id: "convert-API-keys-to-Ethereum-IDs-1") {
		createTable(tableName: "tmp_apikey_lookup") {
			column(name: "key_id", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
			column(name: "user_id", type: "BIGINT(20)") {
				constraints(nullable: "true")
			}
			column(name: "key_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
			column(name: "account_address", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
			column(name: "generated_integration_key_id", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
			column(name: "generated_user_password", type: "VARCHAR(255)") {
				constraints(nullable: "true")
			}
		}
		createIndex(tableName: "tmp_apikey_lookup", indexName: "tmp_apikey_lookup_index_1", unique: "true") {
			column(name: "key_id")
		}
		createIndex(tableName: "tmp_apikey_lookup", indexName: "tmp_apikey_lookup_index_2", unique: "false") {
			column(name: "user_id")
		}
		createIndex(tableName: "tmp_apikey_lookup", indexName: "tmp_apikey_lookup_index_3", unique: "true") {
			column(name: "account_address")
		}
		grailsChange {
			change {
				def THREAD_COUNT = Runtime.getRuntime().availableProcessors() + 1
				ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT)
				def migrations = []
				sql.eachRow('SELECT id, name, user_id FROM `key`') { row ->
					String apiKeyId = row['id']
					String apiKeyName = row['name']
					Long userId = row['user_id']
					migrations << [
						apiKeyId: apiKeyId,
						apiKeyName: apiKeyName,
						userId: userId,
						accountAddressProducer: executorService.submit(new GetAccountAddressTask(apiKeyId)),
					]
				}
				sql.withBatch('INSERT INTO tmp_apikey_lookup (key_id, key_name, user_id, account_address, generated_integration_key_id, generated_user_password) VALUES (?, ?, ?, ?, ?, ?)') { statement ->
					migrations.each { migration ->
						statement.addBatch([
							migration.apiKeyId,
							migration.apiKeyName,
							migration.userId,
							migration.accountAddressProducer.get(),
							IdGenerator.get(),
							(migration.userId == null) ? AlphanumericStringGenerator.getRandomAlphanumericString(32) : null
						])
					}
				}
			}
		}
	}

	// create new users for anonymous keys (and update lookup table)
	changeSet(author: "teogeb", id: "convert-API-keys-to-Ethereum-IDs-2") {
		grailsChange {
			change {
				sql.execute("""
					INSERT INTO user (version, account_expired, account_locked, enabled, name, password, password_expired, username, signup_method)
					SELECT
						0,
						false,
						false,
						true,
						'Anonymous User',
						generated_user_password,
						false,
						account_address,
						?
					FROM tmp_apikey_lookup
					WHERE user_id is null
				""", [SignupMethod.MIGRATED.name()])
				sql.execute("""
					UPDATE tmp_apikey_lookup
					LEFT JOIN user ON user.username = tmp_apikey_lookup.account_address
					SET
						tmp_apikey_lookup.user_id = user.id
					WHERE user_id is null
				""");
			}
		}
	}

	// move the permissions of anonymous keys
	changeSet(author: "teogeb", id: "convert-API-keys-to-Ethereum-IDs-3") {
		grailsChange {
			change {
				sql.execute("""
					UPDATE permission
					LEFT JOIN tmp_apikey_lookup ON tmp_apikey_lookup.key_id = permission.key_id
					SET
						permission.user_id = tmp_apikey_lookup.user_id,
						permission.key_id = null
					WHERE permission.key_id is not null
				""");
			}
		}
	}

	// create integration keys
	changeSet(author: "teogeb", id: "convert-API-keys-to-Ethereum-IDs-4") {
		grailsChange {
			change {
				sql.execute("""
					INSERT INTO integration_key (id, version, name, json, user_id, service, id_in_service, date_created, last_updated)
					SELECT
						generated_integration_key_id,
						0,
						CONCAT('Converted from API key: ',key_name),
						CONCAT(CONCAT('{\"address\":\"',account_address),'\"}'),
						user_id,
						?,
						account_address,
						CURRENT_TIMESTAMP(),
						CURRENT_TIMESTAMP()
					FROM tmp_apikey_lookup
				""", [IntegrationKey.Service.ETHEREUM_ID.name()])
			}
		}
	}

	// delete keys
	changeSet(author: "teogeb", id: "convert-API-keys-to-Ethereum-IDs-5") {
		sql('DELETE FROM `key`')
	}

	// drop lookup table
	changeSet(author: "teogeb", id: "convert-API-keys-to-Ethereum-IDs-6") {
		dropTable(tableName: "tmp_apikey_lookup")
	}
}
