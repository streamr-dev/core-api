package core
databaseChangeLog = {
	changeSet(author: "jtakalai", id: "876-string-to-number-module") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1031)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 27)	// Text
			column(name: "implementing_class", value: "com.unifina.signalpath.text.StringToNumber")
			column(name: "name", value: "StringToNumber")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{"in":"input string"},"inputNames":["in"],"outputs":{"out":"number parsed from input string","error":"Error if input can\'t be parsed"},"outputNames":["out","error"],"helpText":"' +
				'<p>Parse a number from the input string.</p>' +
				'<p>Examples of valid floating-point numbers:</p><ul>' +
					'<li>&quot;1&quot;</li>' +
					'<li>&quot;3.14159&quot;</li>' +
					'<li>&quot;-.234e4&quot; (outputs -2340)</li>' +
					'<li>&quot;+3.e1&quot; (outputs 30)</li></ul>"}')
			column(name: "alternative_names", value: "Parse")
		}
	}
}
