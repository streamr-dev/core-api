package core
databaseChangeLog = {
	changeSet(author: "jtakalai", id: "2016031801409-3") {
		insert(tableName: "module_category") {
			column(name: "id", valueNumeric: 1000)
			column(name: "version", valueNumeric: 0)
			column(name: "name", value: "Integrations")
			column(name: "sort_order", valueNumeric: 130)
			column(name: "module_package_id", valueNumeric: 1)
		}
	}

	changeSet(author: "jtakalai", id: "2016031801409-1") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1000)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 1000)
			column(name: "implementing_class", value: "com.unifina.signalpath.remote.SimpleHttp")
			column(name: "name", value: "Simple HTTP")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 5)
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

	changeSet(author: "jtakalai", id: "2016031801409-2") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1001)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 1000)
			column(name: "implementing_class", value: "com.unifina.signalpath.remote.Http")
			column(name: "name", value: "HTTP Request")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{' +
				'"params":{' +
					'"verb":"HTTP verb (e.g. GET, POST)",' +
					'"URL":"URL to send the request to",' +
					'"params":"Query parameters added to URL (?name=value)",' +
					'"headers":"HTTP Request headers"},' +
				'"paramNames":["verb","URL","params","headers"],' +
				'"inputs":{' +
					'"body":"Request body",' +
					'"trigger":"Send request when input arrives"},' +
				'"inputNames":["body","trigger"],' +
				'"outputs":{' +
					'"errors":"Empty list if all went correctly",' +
					'"data":"Server response payload",' +
					'"status code":"200..299 means all went correctly",' +
					'"ping(ms)":"Round-trip response time in milliseconds",' +
					'"headers":"HTTP Response headers"},' +
				'"outputNames":["errors","data","status code","ping(ms)","headers"],' +
				'"helpText":"' +
					'<p>HTTP Request module sends inputs as HTTP request to given URL, and returns server response.</p>' +
					'<p>Headers, query params and body should be Maps. Body can also be List or String.</p>' +
					'<p>Request body format can be changed in options (wrench icon). Default is JSON. Server is expected to return JSON formatted documents.</p>' +
					'<p>HTTP Request is asynchronous by default. Synchronized requests block the execution of the whole canvas until they receive the server response, but otherwise they work just like any other module; asynchronous requests on the other hand work like streams in that they activate modules they&#39;re connected to only when they receive data from the server. </p>' +
					'<ul><li>If a data path branches, and one branch passes through the HTTP Request module and another around it, if they also converge in a module, that latter module may experience multiple activations due to asynchronicity.</li>' +
					'<li>Asynchronicity also means that server responses may arrive in different order than they were sent.</li>' +
					'<li>If this kind of behaviour causes problems, you can try to fix it by changing sync mode to <i>synchronized</i> in options (wrench icon). ' +
					'<ul><li>Caveat: data throughput WILL be lower, and external servers may freeze your canvas simply by responding very slowly (or not at all).</li></ul></li>' +
					'<li>For simple data paths and somewhat stable response times, the two sync modes will yield precisely the same results.</li></ul>'
			)
		}
	}
}
