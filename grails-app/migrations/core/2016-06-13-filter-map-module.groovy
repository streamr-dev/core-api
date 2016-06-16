package core
databaseChangeLog = {
	changeSet(author: "eric", id: "filter-map-module") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 525)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 51)
			column(name: "implementing_class", value: "com.unifina.signalpath.map.FilterMap")
			column(name: "name", value: "FilterMap")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{"keys":"if empty, keep all entries. otherwise filter by given keys."},"paramNames":["keys"],"inputs":{"in":"map to be filtered"},"inputNames":["in"],"outputs":{"out":"filtered map"},"outputNames":["out"],"helpText":"<p>Filter incoming maps by retaining entries with specified keys.</p>"}')
		}
	}
}
