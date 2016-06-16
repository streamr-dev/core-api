package core
databaseChangeLog = {

	changeSet(author: "eric", id: "new-event-table-module-1") {
		sql("UPDATE module SET hide = true, name = 'Table (old)' WHERE id = 142")
	}

	changeSet(author: "eric", id: "new-event-table-module-2") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 527)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 3)
			column(name: "implementing_class", value: "com.unifina.signalpath.utils.VariadicEventTable")
			column(name: "name", value: "Table")
			column(name: "js_module", value: "TableModule")
			column(name: "type", value: "module event-table-module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{},"inputNames":[],"outputs":{},"outputNames":[],"helpText":"<p>Displays a table of events arriving at the inputs along with their timestamps. The number of inputs can be adjusted in module options. Every input corresponds to a table column. Very useful for debugging and inspecting values. The inputs can be connected to all types of outputs.</p>"}')
			column(name: "alternative_names", value: "Events")
			column(name: "webcomponent", value: "streamr-table")
		}
	}
}
