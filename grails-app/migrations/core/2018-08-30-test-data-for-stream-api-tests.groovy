package core

databaseChangeLog = {
	changeSet(author: "eric", id: "test-data-for-stream-api-tests-1", context: "test") {

		int userId = 7

		insert(tableName: "sec_user") {
			column(name: "id", valueNumeric: userId)
			column(name: "version", valueNumeric: 0)
			column(name: "account_expired", valueNumeric: 0)
			column(name: "account_locked", valueNumeric: 0)
			column(name: "enabled", valueNumeric: 1)
			column(name: "name", value: "Stream API Test User")
			column(name: "password", value: "")
			column(name: "password_expired", valueNumeric: 0)
			column(name: "timezone", value: "UTC")
			column(name: "username", value: "stream-api-tester@streamr.com")
		}

		insert(tableName: "key") {
			column(name: "id", value: "stream-api-tester-key")
			column(name: "version", valueNumeric: 0)
			column(name: "name", value: "Default")
			column(name: "user_id", valueNumeric: userId)
		}
	}

	changeSet(author: "eric", id: "test-data-for-stream-api-tests-2", context: "test") {

		int userId = 8

		insert(tableName: "sec_user") {
			column(name: "id", valueNumeric: userId)
			column(name: "version", valueNumeric: 0)
			column(name: "account_expired", valueNumeric: 0)
			column(name: "account_locked", valueNumeric: 0)
			column(name: "enabled", valueNumeric: 1)
			column(name: "name", value: "Stream API Test User 2")
			column(name: "password", value: "")
			column(name: "password_expired", valueNumeric: 0)
			column(name: "timezone", value: "UTC")
			column(name: "username", value: "stream-api-tester-2@streamr.com")
		}

		insert(tableName: "key") {
			column(name: "id", value: "stream-api-tester2-key")
			column(name: "version", valueNumeric: 0)
			column(name: "name", value: "Default")
			column(name: "user_id", valueNumeric: userId)
		}
	}
}