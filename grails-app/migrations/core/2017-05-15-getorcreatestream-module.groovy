package core
databaseChangeLog = {
	changeSet(author: "jtakalai", id: "2017-05-15-getorcreatestream-module") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1033)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 53) // Streams
			column(name: "implementing_class", value: "com.unifina.signalpath.streams.GetOrCreateStream")
			column(name: "name", value: "GetOrCreateStream")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{"fields":"the fields to be assigned to the stream if a new stream is created"},"paramNames":["fields"],"inputs":{"name":"name of the stream","description":"human-readable description if a new stream is created"},"inputNames":["name","description"],"outputs":{"created":"true if stream was created, false if existing stream was found","stream":"the id of the found or created stream"},"outputNames":["created","stream"],"helpText":"<p>Find existing stream by name, or create a new stream if a stream by that name doesn\'t exist yet. If a stream is found, <i>fields</i> and <i>description</i> inputs are <b>ignored</b>.</p>"}')
			column(name: "alternative_names", value: "StreamByName")
		}
	}
}