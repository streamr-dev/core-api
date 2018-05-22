package core

databaseChangeLog = {
	changeSet(author: "eric", id: "test-data-for-product-api-1", context: "test") {

		int userId = 4

		insert(tableName: "sec_user") {
			column(name: "id", valueNumeric: userId)
			column(name: "version", valueNumeric: 0)
			column(name: "account_expired", valueNumeric: 0)
			column(name: "account_locked", valueNumeric: 0)
			column(name: "enabled", valueNumeric: 1)
			column(name: "name", value: "Product API Test User")
			column(name: "password", value: "")
			column(name: "password_expired", valueNumeric: 0)
			column(name: "timezone", value: "UTC")
			column(name: "username", value: "product-api-tester@streamr.com")
		}

		insert(tableName: "key") {
			column(name: "id", value: "product-api-tester-key")
			column(name: "version", valueNumeric: 0)
			column(name: "name", value: "Default")
			column(name: "user_id", valueNumeric: userId)
		}
	}

	changeSet(author: "eric", id: "test-data-for-product-api-2", context: "test") {

		int userId = 5

		insert(tableName: "sec_user") {
			column(name: "id", valueNumeric: userId)
			column(name: "version", valueNumeric: 0)
			column(name: "account_expired", valueNumeric: 0)
			column(name: "account_locked", valueNumeric: 0)
			column(name: "enabled", valueNumeric: 1)
			column(name: "name", value: "Product API Test User 2")
			column(name: "password", value: "")
			column(name: "password_expired", valueNumeric: 0)
			column(name: "timezone", value: "UTC")
			column(name: "username", value: "product-api-tester-2@streamr.com")
		}

		insert(tableName: "key") {
			column(name: "id", value: "product-api-tester2-key")
			column(name: "version", valueNumeric: 0)
			column(name: "name", value: "Default")
			column(name: "user_id", valueNumeric: userId)
		}
	}

	changeSet(author: "eric", id: "test-data-for-product-api-3", context: "test") {

		int userId = 6

		insert(tableName: "sec_user") {
			column(name: "id", valueNumeric: userId)
			column(name: "version", valueNumeric: 0)
			column(name: "account_expired", valueNumeric: 0)
			column(name: "account_locked", valueNumeric: 0)
			column(name: "enabled", valueNumeric: 1)
			column(name: "name", value: "Product API Test User")
			column(name: "password", value: "")
			column(name: "password_expired", valueNumeric: 0)
			column(name: "timezone", value: "UTC")
			column(name: "username", value: "devops-user@streamr.com")
		}

		insert(tableName: "key") {
			column(name: "id", value: "devops-user-key")
			column(name: "version", valueNumeric: 0)
			column(name: "name", value: "Default")
			column(name: "user_id", valueNumeric: userId)
		}

		insert(tableName: "sec_user_sec_role") {
			column(name: "sec_role_id", valueNumeric: 7) // ROLE_DEV_OPS
			column(name: 'sec_user_id', valueNumeric: userId)
		}
	}

	changeSet(author: "eric", id: "test-data-for-product-api-4", context: "test") {
		insert(tableName: "category") {
			column(name: "id", value: "automobile-id")
			column(name: "name", value: "Automobile")
			column(name: "image_url", value: "http://localhost:8081/streamr-core/uploads/auto.png")
		}

		insert(tableName: "category") {
			column(name: "id", value: "ad-id")
			column(name: "name", value: "Advertising")
		}

		insert(tableName: "category") {
			column(name: "id", value: "cryptocurrencies-id")
			column(name: "name", value: "Cryptocurrency")
			column(name: "image_url", value: "http://localhost:8081/streamr-core/uploads/crypto.png")
		}

		insert(tableName: "category") {
			column(name: "id", value: "financial-id")
			column(name: "name", value: "Financial")
			column(name: "image_url", value: "http://localhost:8081/streamr-core/uploads/finance.png")
		}

		insert(tableName: "category") {
			column(name: "id", value: "personal-id")
			column(name: "name", value: "Personal")
		}

		insert(tableName: "category") {
			column(name: "id", value: "satellite-id")
			column(name: "name", value: "Satellite")
			column(name: "image_url", value: "http://localhost:8081/streamr-core/uploads/satellites-in-space-680px.png")
		}
	}
}