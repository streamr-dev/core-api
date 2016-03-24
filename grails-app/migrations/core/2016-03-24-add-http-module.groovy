package core
databaseChangeLog = {
	changeSet(author: "jtakalai", id: "2016032401824-1") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1001)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 3)
			column(name: "implementing_class", value: "com.unifina.signalpath.remote.Http")
			column(name: "name", value: "HTTP Request")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{' +
				'"params":{"verb":"HTTP verb (e.g. GET, POST)","URL":"URL to send the request to"},' +
				'"paramNames":["verb","URL"],' +
				'"inputs":{"body":"Request body","trigger":"Send request when input arrives"},' +
				'"inputNames":["body","trigger"],' +
				'"outputs":{"error":"Description of what went wrong"},' +
				'"outputNames":["error"],' +
				'"helpText":"' +
					'<p>HTTP Request module sends inputs as HTTP request to given URL, and returns server response.</p>' +
					'<p>Headers, query params and body should be Maps. Body can also be List or String.</p>' +
			'"}')
		}
	}
}
