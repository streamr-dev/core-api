package core
databaseChangeLog = {
	changeSet(author: "eric", id: "add-stream-modules-1") {
		insert(tableName: "module_category") {
			column(name: "id", valueNumeric: 53)
			column(name: "version", valueNumeric: 0)
			column(name: "name", value: "Streams")
			column(name: "sort_order", valueNumeric: 143)
			column(name: "module_package_id", valueNumeric: 1)
		}
	}

	changeSet(author: "eric", id: "add-stream-modules-2") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 528)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 53)
			column(name: "implementing_class", value: "com.unifina.signalpath.streams.SearchStream")
			column(name: "name", value: "SearchStream")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{"name":"stream to search for by name, must be exact"},"inputNames":["name"],"outputs":{"found":"true if stream was found","stream":"id of stream if found"},"outputNames":["found","stream"],"helpText":"<p>Search for a stream by name</p>"}')
		}
	}

	changeSet(author: "eric", id: "add-stream-modules-3") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 529)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 53)
			column(name: "implementing_class", value: "com.unifina.signalpath.streams.CreateStream")
			column(name: "name", value: "CreateStream")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{"fields":"the fields to be assigned to the stream"},"paramNames":["fields"],"inputs":{"name":"name of the stream","description":"human-readable description"},"inputNames":["name","description"],"outputs":{"created":"true if stream was created, false if failed to create stream","stream":"the id of the created stream"},"outputNames":["created","stream"],"helpText":"<p>Create a new stream.</p>"}')
		}
	}
}