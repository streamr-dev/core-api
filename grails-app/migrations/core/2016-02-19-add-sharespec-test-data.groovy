package core

databaseChangeLog = {

	changeSet(author: "jtakalai", context: "test", id: "2016021931337-1") {
		// date format specified in http://www.liquibase.org/documentation/column.html
		String now = "2016-02-22T15:00:00"
		insert(tableName: "stream") {
			//column(name: "id", valueNumeric: autoincremented?)
			column(name: "version", valueNumeric: 0)
			column(name: "api_key", value: "share-spec--stream-key")
			column(name: "description", value: "Test share buttons and dialogs")
			column(name: "feed_id", valueNumeric: 7)    // UserStream
			column(name: "name", value: "ShareSpec")
			column(name: "config", value: '{"fields":[],"topic":"4nxQHjdNQVmy551UB6S4cQ"}')
			column(name: "user_id", valueNumeric: 1)    // tester1@streamr.com
			column(name: "uuid", value: "share-spec-stream-uuid")
			column(name: "class", value: "com.unifina.domain.data.Stream")
		}
		insert(tableName: "canvas") {
			column(name: "id", value: "share-spec-canvas-uuid")
			column(name: "name", value: "ShareSpec")
			column(name: "state", value: "stopped")
			column(name: "date_created", valueDate: now)
			column(name: "last_updated", valueDate: now)
			column(name: "user_id", valueNumeric: 1)   // tester1@streamr.com
			column(name: "version", valueNumeric: 0)
			column(name: "adhoc", valueBoolean: false)
			column(name: "example", valueBoolean: false)
			column(name: "shared", valueBoolean: false)
			column(name: "has_exports", valueBoolean: false)
			column(name: "json", value: '{"settings":{"speed":"0","timeOfDayFilter":{"timeZoneOffset":120,"timeOfDayStart":"00:00:00","timeZoneDst":true,"timeOfDayEnd":"23:59:00","timeZone":"Europe/Helsinki"},"endDate":"2015-07-03","beginDate":"2015-07-02"},"name":"CanvasSpec test loading a SignalPath","uiChannel":{},"modules":[]}')
//.replace('"', '\\"'))
		}
		insert(tableName: "dashboard") {
			//column(name: "id", valueNumeric: autoincremented?)
			column(name: "version", valueNumeric: 0)
			column(name: "date_created", valueDate: now)
			column(name: "last_updated", valueDate: now)
			column(name: "name", value: "ShareSpec")
			column(name: "user_id", valueNumeric: 1)    // tester1@streamr.com
		}
	}

}
