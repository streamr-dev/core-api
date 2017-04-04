package core
databaseChangeLog = {

	changeSet(author: "eric", id: "migrate-stream-api-keys-to-key-domain-1") {
		grailsChange {
			change {
				sql.eachRow("SELECT id, api_key FROM stream") { row ->
					def streamId = row['id']
					def keyId = row['api_key']

					sql.execute('INSERT INTO `key` (id, version, name, user_id) VALUES (?, 0, ?, NULL)', keyId, 'Generated')
					sql.execute('INSERT INTO permission (version, clazz, operation, string_id, anonymous, key_id) VALUES (0, "com.unifina.domain.data.Stream", "read", ?, 0, ?)',
						streamId, keyId
					)
					sql.execute('INSERT INTO permission (version, clazz, operation, string_id, anonymous, key_id) VALUES (0, "com.unifina.domain.data.Stream", "write", ?, 0, ?)',
						streamId, keyId
					)
				}
			}
		}
	}

	changeSet(author: "eric", id: "migrate-stream-api-keys-to-key-domain-2") {
		dropColumn(columnName: "api_key", tableName: "stream")
	}
}
