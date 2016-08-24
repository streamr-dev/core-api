package core
databaseChangeLog = {
	changeSet(author: "eric", id: "each-with-index-1") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 541)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.EachWithIndex")
			column(name: "name", value: "EachWithIndex")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
		}
	}
}