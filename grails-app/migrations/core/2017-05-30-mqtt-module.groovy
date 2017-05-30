package core
databaseChangeLog = {
	changeSet(author: "jtakalai", id: "449-mqtt-module") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1034)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 1000)
			column(name: "implementing_class", value: "com.unifina.signalpath.remote.Mqtt")
			column(name: "name", value: "MQTT")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{"URL":"URL of MQTT broker to listen to","topic":"MQTT topic"},"paramNames":["URL","topic"],"inputs":{},"inputNames":[],"outputs":{"message":"MQTT message string"},"outputNames":["message"],"helpText":"<p>Listen to MQTT messages, output them as strings. If message is JSON, a JsonParser module can be used to transform the string into a map, and GetMultiFromMap module to extract values from the map.</p>"}')
		}
	}
}
