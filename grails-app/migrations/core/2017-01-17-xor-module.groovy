package core
databaseChangeLog = {
	changeSet(author: "eric", id: "xor-module") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 573)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 10) // Boolean
			column(name: "implementing_class", value: "com.unifina.signalpath.bool.Xor")
			column(name: "name", value: "Xor")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{},"inputNames":[],"outputs":{},"outputNames":[],"helpText":"<p>Implements the boolean XOR operation: outputs true&nbsp;if <span class=\\\\"highlight\\\\">one</span> of the inputs equal true, otherwise outputs false.</p>"}')
		}
	}
}