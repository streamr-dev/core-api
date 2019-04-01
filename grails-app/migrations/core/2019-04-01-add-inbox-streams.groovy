package core

import com.unifina.domain.data.Stream
import com.unifina.utils.EthereumAddressValidator

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
				sql.eachRow('SELECT id, username FROM sec_user') { row ->
					String id = row['id']
					String username = row['username']
					if (EthereumAddressValidator.validate(username)) {
						Date d = new Date()
						sql.execute('INSERT INTO stream (version, feed_id, id, name, date_created, last_updated, partitions, require_signed_data, auto_configure, storage_days, inbox)' +
							' VALUES (0, 7, ?, ?, ?, ?, 1, b\'0\', b\'1\', ?, b\'1\')', username, username, d, d, Stream.DEFAULT_STORAGE_DAYS)
						sql.execute('INSERT INTO permission (version, operation, stream_id, anonymous, user_id) VALUES (0, "read", ?, b\'0\', ?)',
							username, id
						)
						sql.execute('INSERT INTO permission (version, operation, stream_id, anonymous, user_id) VALUES (0, "write", ?, b\'0\', ?)',
							username, id
						)
						sql.execute('INSERT INTO permission (version, operation, stream_id, anonymous, user_id) VALUES (0, "share", ?, b\'0\', ?)',
							username, id
						)
					}
				}
			}
		}
	}
}
