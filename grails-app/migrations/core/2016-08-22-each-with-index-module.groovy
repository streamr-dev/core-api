package core
databaseChangeLog = {
	changeSet(author: "eric", id: "each-with-index-1") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 541)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.Indices")
			column(name: "name", value: "Indices")
			column(name: "alternative_names", value: "Indexes")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{"list":"an input list"},"inputNames":["list"],"outputs":{"indices":"a list of indices for the input list","list":"the original input list"},"outputNames":["indices","list"],"helpText":"<p>Generates a list from <strong>[0,n-1]</strong>&nbsp;according to the size <strong>n</strong>&nbsp;of the given input list.&nbsp;</p>"}')
		}
	}
}