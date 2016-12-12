package core
databaseChangeLog = {
	changeSet(author: "eric", id: "pass-complete-through-module") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 572)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 3) // Utils
			column(name: "implementing_class", value: "com.unifina.signalpath.utils.PassCompleteThrough")
			column(name: "name", value: "PassCompleteThrough")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{},"inputNames":[],"outputs":{},"outputNames":[],"helpText":"<p>Simply passes the values of inputs&nbsp;to corresponding outputs, but only&nbsp;if <strong>all</strong> inputs have received a value. If one or more inputs have not received a value before module activation, no value is sent forward.</p>"}')
		}
	}
}