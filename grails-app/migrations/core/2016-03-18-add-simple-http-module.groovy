package core
databaseChangeLog = {
	changeSet(author: "jtakalai", id: "2016031801409-1") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1000)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 3)
			column(name: "implementing_class", value: "com.unifina.signalpath.remote.SimpleHttp")
			column(name: "name", value: "Simple HTTP")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{' +
				'"params":{"verb":"HTTP verb (e.g. GET, POST)","URL":"URL to send the request to"},' +
				'"paramNames":["verb","URL"],' +
				'"inputs":{"trigger":"Send request when input arrives"},' +
				'"inputNames":["trigger"],' +
				'"outputs":{"error":"Description of what went wrong"},' +
				'"outputNames":["error"],' +
				'"helpText":"' +
					'<p>HTTP Request module sends input values as HTTP request to given URL, parses the server ' +
						'response, and sends resulting values through named outputs.</p>' +
					'<p>Please rename inputs, outputs and headers using names that the target API requires. ' +
						'To pluck values nested deeper in response JSON, use square brackets and dot notation, e.g. ' +
						'naming output as <i>customers[2].name</i> would fetch "Bob" from ' +
						'<i>{"customers":[{"name":"Rusty"},{"name":"Mack"},{"name":"</i><b>Bob</b><i>"}]}</i> ' +
						'(array indices are <b>zero</b>-based, that is, first element is number <b>0</b>!)</p>' +
					'<p>For GET and DELETE requests, the input values are added to URL parameters:<br />' +
					'<i>http://url?key1=value1&key2=value2&...</i></p>' +
					'<p>For other requests, the input values are sent in the body as JSON object:<br />' +
					'<i>{"key1": "value1", "key2": "value2", ...}</i></p>' +
			'"}')
		}
	}
}
