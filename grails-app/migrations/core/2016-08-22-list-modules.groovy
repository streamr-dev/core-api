package core
databaseChangeLog = {
	changeSet(author: "eric", id: "list-modules-1") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 539)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.ForEachItem")
			column(name: "name", value: "ForEachItem")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
		}
	}

	changeSet(author: "eric", id: "list-modules-2") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 540)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.RepeatItem")
			column(name: "name", value: "RepeatItem")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
		}
	}
}