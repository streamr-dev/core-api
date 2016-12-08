package core
databaseChangeLog = {
	changeSet(author: "eric", id: "time-of-event-module") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 566)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 28) // Time & Date
			column(name: "implementing_class", value: "com.unifina.signalpath.time.TimeOfEvent")
			column(name: "name", value: "TimeOfEvent")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{"trigger":"any value; causes module to activate, i.e., produce output"},"inputNames":["trigger"],"outputs":{"timestamp":"time of the current event"},"outputNames":["timestamp"],"helpText":"<p>Get timestamp for the event currently being processed. Similar to <strong>Clock,&nbsp;</strong>but instead of generating events,&nbsp;this&nbsp;module is triggered manually through input&nbsp;<em>trigger</em>.&nbsp;</p>"}')
		}
	}
}