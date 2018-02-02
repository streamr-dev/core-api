package core
databaseChangeLog = {
	changeSet(author: "jarno", id: "mqtt-help-text-update-1") {
		update(tableName: "module") {
			column(name: "json_help", value: '{"params":' +
					'{"URL":"URL of MQTT broker to listen to",' +
					'"topic":"MQTT topic",' +
					'"username":"MQTT username (optional)",' +
					'"password":"MQTT password (optional)",' +
					'"certType":"MQTT certificate type"},' +
					'"paramNames":["URL","topic","username","password","certType"],' +
					'"inputs":{},' +
					'"inputNames":[],' +
					'"outputs":{"message":"MQTT message string"},' +
					'"outputNames":["message"],' +
					'"helpText":"' +
						'<p>Listen to MQTT messages, output them as strings. If message is JSON, a JsonParser module can be used to transform the string into a map, and GetMultiFromMap module to extract values from the map.</p>' +
						'<h2>Examples:</h2>' +
						'<h3>Connecting to MQTT service without certificate</h3>' +
						'<p>Give URL address as</p><pre>mqtt://service.com</pre><p>or</p><pre>tcp://service.com</pre><p>Add topic and username and password if needed.</p>' +
						'<h3>Connecting to MQTT with certificate</h3>' +
						'<p>Give URL address as</p><pre>ssl://service.com</pre><p>Add topic and username and password if needed.</p><p>Select certificate type to be .crt and paste your certificate to text area.</p>"}')
			where("id = 1034")
		}
	}
}