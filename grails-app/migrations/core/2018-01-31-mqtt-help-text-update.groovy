package core
databaseChangeLog = {
	changeSet(author: "jarno", id: "1517398287420-1") {
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
					'"helpText":"<p>Listen to MQTT messages, output them as strings. If message is JSON, a JsonParser module can be used to transform the string into a map, and GetMultiFromMap module to extract values from the map.</p>\\n\\n<h2>Examples:</h2>\\n\\n<h3>Connecting to MQTT service without certificate</h3>\\n\\n<p>Give URL address as</p>\\n\\n<pre>\\nmqtt://service.com</pre>\\n\\n<p>or</p>\\n\\n<pre>\\ntcp://service.com</pre>\\n\\n<p>Add topic and username and password if needed.</p>\\n\\n<h3>Connecting to MQTT with certificate</h3>\\n\\n<p>Give URL address as</p>\\n\\n<pre>\\nssl://service.com</pre>\\n\\n<p>&nbsp;</p>\\n\\n<p>Add topic and username and password if needed.</p>\\n\\n<p>Select certificate type to be .crt and paste your certificate to text area.</p>\\n\\n<p>&nbsp;</p>\\n\\n<p>&nbsp;</p>\\n"}')
			where("id = 1034")
		}
	}
}