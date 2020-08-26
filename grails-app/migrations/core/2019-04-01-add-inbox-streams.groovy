package core

import com.unifina.domain.Stream

databaseChangeLog = {
	changeSet(author: "mthambipillai", id: "add-inbox-field-1") {
		addColumn(tableName: "stream") {
			column(name: "inbox", type: "bit") {
				constraints(nullable: "false")
			}
		}
		addNotNullConstraint(columnDataType: "bit", columnName: "inbox", defaultNullValue: "0", tableName: "stream")
	}
	changeSet(author: "mthambipillai", id: "create-inbox-stream-for-ethereum-users") {
		grailsChange {
			change {
				sql.eachRow('SELECT DISTINCT id_in_service FROM integration_key WHERE service = \"ETHEREUM_ID\" OR service = \"ETHEREUM\"') { row ->
					String ethereumAddress = row['id_in_service']
					Date d = new Date()
					sql.execute('INSERT INTO stream (version, feed_id, id, name, date_created, last_updated, partitions, require_signed_data, auto_configure, storage_days, inbox)' +
						' VALUES (0, 7, ?, ?, ?, ?, 1, b\'0\', b\'0\', ?, b\'1\')', ethereumAddress, ethereumAddress, d, d, Stream.DEFAULT_STORAGE_DAYS)
					sql.eachRow('SELECT user_id FROM integration_key WHERE id_in_service = :address', [address:ethereumAddress]) { userRow ->
						String userId = userRow['user_id']
						sql.execute('INSERT INTO permission (version, operation, stream_id, anonymous, user_id) VALUES (0, "read", ?, b\'0\', ?)',
							ethereumAddress, userId
						)
						sql.execute('INSERT INTO permission (version, operation, stream_id, anonymous, user_id) VALUES (0, "write", ?, b\'0\', ?)',
							ethereumAddress, userId
						)
						sql.execute('INSERT INTO permission (version, operation, stream_id, anonymous, user_id) VALUES (0, "share", ?, b\'0\', ?)',
							ethereumAddress, userId
						)
					}
				}
			}
		}
	}
}
