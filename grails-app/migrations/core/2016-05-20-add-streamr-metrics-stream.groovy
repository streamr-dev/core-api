package core
databaseChangeLog = {
	// date format specified in http://www.liquibase.org/documentation/column.html
	String now = "2016-05-20T13:00:00"

	// Stream where "metrics events" are reported, see http://dev.streamr:8090/browse/CORE-621
	changeSet(author: "jtakalai", id: "20160520-1233-1") {
		insert(tableName: "stream") {
			column(name: "id", value: "streamr-metrics")
			column(name: "version", valueNumeric: 10)
			column(name: "api_key", value: "s4lainen-m3triikka-4vain")
			column(name: "description", value: "Internal metrics events")
			column(name: "feed_id", valueNumeric: 7)    // UserStream
			column(name: "name", value: "StreamrMetrics")
			column(name: "config", value: '{' +
				'"topic":"streamr-metrics",' +
				'"fields":[' +
					'{"name":"metric","type":"string"},' +
					'{"name":"value","type":"number"},' +
					'{"name":"user","type":"number"}' +
				']}')
			column(name: "user_id", valueNumeric: 1)
			column(name: "class", value: "com.unifina.domain.data.Stream")
			column(name: "date_created", valueDate: now)
			column(name: "last_updated", valueDate: now)
		}
	}
}
