package core

databaseChangeLog = {
	changeSet(author: "aapeli", id: "tester2-testing-dashboard", context: "test") {
		insert(tableName: "dashboard") {
			column(name: "id", valueNumeric: 456456)
			column(name: "version", valueNumeric: 0)
			column(name: "date_created", value: "2016-06-07 00:00:00")
			column(name: "last_updated", value: "2016-06-07 00:00:00")
			column(name: "name", value: "DashboardSpecNotSharedDashboard")
			column(name: "user_id", valueNumeric: 2)
		}
		insert(tableName: "dashboard") {
			column(name: "id", valueNumeric: 567567)
			column(name: "version", valueNumeric: 0)
			column(name: "date_created", value: "2016-06-07 00:00:00")
			column(name: "last_updated", value: "2016-06-07 00:00:00")
			column(name: "name", value: "DashboardSpecReadSharedDashboard")
			column(name: "user_id", valueNumeric: 2)
		}
		insert(tableName: "dashboard") {
			column(name: "id", valueNumeric: 678678)
			column(name: "version", valueNumeric: 0)
			column(name: "date_created", value: "2016-06-07 00:00:00")
			column(name: "last_updated", value: "2016-06-07 00:00:00")
			column(name: "name", value: "DashboardSpecShareSharedDashboard")
			column(name: "user_id", valueNumeric: 2)
		}
		insert(tableName: "permission") {
			column(name: "version", valueNumeric: 0)
			column(name: "clazz", value: "com.unifina.domain.dashboard.Dashboard")
			column(name: "long_id", valueNumeric: 567567)
			column(name: "operation", value: "read")
			column(name: "user_id", valueNumeric: 1)
			column(name: "anonymous", valueNumeric: 0)
		}
		insert(tableName: "permission") {
			column(name: "version", valueNumeric: 0)
			column(name: "clazz", value: "com.unifina.domain.dashboard.Dashboard")
			column(name: "long_id", valueNumeric: 678678)
			column(name: "operation", value: "share")
			column(name: "user_id", valueNumeric: 1)
			column(name: "anonymous", valueNumeric: 0)
		}
		insert(tableName: "permission") {
			column(name: "version", valueNumeric: 0)
			column(name: "clazz", value: "com.unifina.domain.dashboard.Dashboard")
			column(name: "long_id", valueNumeric: 678678)
			column(name: "operation", value: "read")
			column(name: "user_id", valueNumeric: 1)
			column(name: "anonymous", valueNumeric: 0)
		}
	}
}