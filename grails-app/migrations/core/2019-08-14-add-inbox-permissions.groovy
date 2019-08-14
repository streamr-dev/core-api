package core

databaseChangeLog = {
	changeSet(author: "mthambipillai", id: "adding-permissions-to-inbox-streams") {
		grailsChange {
			change {
				sql.eachRow('SELECT DISTINCT id_in_service FROM integration_key WHERE service = \"ETHEREUM_ID\" OR service = \"ETHEREUM\"') { row ->
					String publisherInboxAddress = row['id_in_service']
					sql.eachRow('SELECT user_id FROM integration_key WHERE id_in_service = :address', [address:publisherInboxAddress]) { userRow ->
						// for each user with an inbox stream
						String publisherId = userRow['user_id']
						sql.eachRow('SELECT stream_id FROM permission WHERE stream_id IS NOT NULL AND user_id = :user_id AND operation = \"write\" AND inbox = b\'0\'', [user_id:publisherId]) { streamRow ->
							// for each stream to which the user can publish
							String streamId = streamRow['stream_id']
							sql.eachRow('SELECT user_id FROM permission WHERE stream_id = :stream_id AND operation = \"read\"', [stream_id:streamId]) { subscriberRow ->
								// for each subscriber of that stream
								String subscriberId = subscriberRow['user_id']
								sql.eachRow('SELECT DISTINCT id_in_service FROM integration_key WHERE (service = \"ETHEREUM_ID\" OR service = \"ETHEREUM\") AND user_id = :subscriber_id', [subscriber_id:subscriberId]) { addressRow ->
									// for each inbox stream of that subscriber
									String subscriberInboxAddress = row['id_in_service']
									// give write permission to each other's stream
									sql.execute('INSERT INTO permission (version, operation, stream_id, anonymous, user_id) VALUES (0, "write", ?, b\'0\', ?)',
										publisherInboxAddress, subscriberId
									)
									sql.execute('INSERT INTO permission (version, operation, stream_id, anonymous, user_id) VALUES (0, "write", ?, b\'0\', ?)',
										subscriberInboxAddress, publisherId
									)
								}
							}
						}
					}
				}
			}
		}
	}
}
