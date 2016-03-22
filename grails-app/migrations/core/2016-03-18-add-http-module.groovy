package core
databaseChangeLog = {
	changeSet(author: "jtakalai", id: "2016031801409-1") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 225)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 3)
			column(name: "implementing_class", value: "com.unifina.signalpath.remote.Http")
			column(name: "name", value: "HTTP request")
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
					'<p>HTTP Request module sends input values as HTTP request to given URL and outputs the response.</p>' +
					'<p>For GET and DELETE requests, the input values are added to URL parameters:<br />' +
					'<i>http://url?key1=value1&amp;key2=value2&amp;...</i></p>' +
					'<p>For other requests, the input values are sent in the body as JSON object:<br />' +
					'<i>{&quot;key1&quot;: &quot;value1&quot;, &quot;key2&quot;: &quot;value2&quot;, ...}</i></p>' +
			'"}')
		}
	}
}
