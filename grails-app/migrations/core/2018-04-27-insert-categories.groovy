package core

databaseChangeLog = {
	changeSet(author: "eric", id: "insert-categories") {
		insert(tableName: "category") {
			column(name: "name", value: "Advertising")
			column(name: "id", value: "advertising")
		}

		insert(tableName: "category") {
			column(name: "name", value: "Business Intelligence")
			column(name: "id", value: "business-intelligence")
		}

		insert(tableName: "category") {
			column(name: "name", value: "Communications")
			column(name: "id", value: "communications")
		}

		insert(tableName: "category") {
			column(name: "name", value: "Crypto")
			column(name: "id", value: "crypto")
		}

		insert(tableName: "category") {
			column(name: "name", value: "Energy")
			column(name: "id", value: "energy")
		}

		insert(tableName: "category") {
			column(name: "name", value: "Environment")
			column(name: "id", value: "environment")
		}

		insert(tableName: "category") {
			column(name: "name", value: "Entertainment")
			column(name: "id", value: "entertainment")
		}

		insert(tableName: "category") {
			column(name: "name", value: "Finance")
			column(name: "id", value: "finance")
		}

		insert(tableName: "category") {
			column(name: "name", value: "Health")
			column(name: "id", value: "health")
		}

		insert(tableName: "category") {
			column(name: "name", value: "Industrial")
			column(name: "id", value: "industrial")
		}

		insert(tableName: "category") {
			column(name: "name", value: "IoT")
			column(name: "id", value: "iot")
		}

		insert(tableName: "category") {
			column(name: "name", value: "Other")
			column(name: "id", value: "other")
		}

		insert(tableName: "category") {
			column(name: "name", value: "Retail")
			column(name: "id", value: "retail")
		}

		insert(tableName: "category") {
			column(name: "name", value: "Smart Cities")
			column(name: "id", value: "smart-cities")
		}

		insert(tableName: "category") {
			column(name: "name", value: "Social Media")
			column(name: "id", value: "social-media")
		}

		insert(tableName: "category") {
			column(name: "name", value: "Sports")
			column(name: "id", value: "sports")
		}

		insert(tableName: "category") {
			column(name: "name", value: "Transportation")
			column(name: "id", value: "transportation")
		}

		insert(tableName: "category") {
			column(name: "name", value: "Weather")
			column(name: "id", value: "weather")
		}
	}
}
