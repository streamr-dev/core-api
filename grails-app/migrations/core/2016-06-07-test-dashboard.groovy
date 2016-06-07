package core

databaseChangeLog = {
	changeSet(author: "aapeli", id: "tester2-testing-dashboard", context: "test") {
		insert(tableName: "dashboard") {
			column(name: "id", valueNumeric: 1000000)
			column(name: "version", valueNumeric: 0)
			column(name: "date_created", value: "2016-06-07 00:00:00")
			column(name: "last_updated", value: "2016-06-07 00:00:00")
			column(name: "name", value: "DashboardSpec")
			column(name: "user_id", valueNumeric: 2)
		}
	}
}