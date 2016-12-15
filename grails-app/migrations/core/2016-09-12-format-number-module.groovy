package core
databaseChangeLog = {
	changeSet(author: "eric", id: "format-number-module-1") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 569)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 27) // Text
			column(name: "implementing_class", value: "com.unifina.signalpath.text.FormatNumber")
			column(name: "name", value: "FormatNumber")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{"decimalPlaces":"number of decimal places"},"paramNames":["decimalPlaces"],"inputs":{"number":"number to format"},"inputNames":["number"],"outputs":{"text":"number formatted as string"},"outputNames":["text"],"helpText":"<p>Format a number into a string with a specified number of&nbsp;decimal places.</p>"}')
		}
	}
}