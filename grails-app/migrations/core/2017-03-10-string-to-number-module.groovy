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
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{"in":"input string"},"inputNames":["in"],"outputs":{"out":"number parsed from input string"},"outputNames":["out"],"helpText":"' +
				'<p>Parse a number from the input string. If <b>strict</b> option is set <strong>true</strong>, only strings that precisely represent a double-precision floating-point&nbsp;number are sent out.</p>' +
				'<p>Examples of valid floating-point numbers:</p><ul>' +
					'<li>&quot;1&quot;</li>' +
					'<li>&quot;3.14159&quot;</li>' +
					'<li>&quot;-.234e4&quot; (outputs -2340)</li>' +
					'<li>&quot;+3.e1&quot; (outputs 30)</li></ul>' +
				'<p>For strings like &quot;$2&quot; or &quot;3 ms&quot;, the number is&nbsp;parsed and sent out only in non-strict mode. Non-strict mode sends out zero if no number was&nbsp;found within the string.</p>"}')
		}
	}
}
