package core
databaseChangeLog = {
	changeSet(author: "jtakalai", id: "870-add-list-to-events-module") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1030)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.ListToEvents")
			column(name: "name", value: "ListToEvents")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{"list":"input list"},"inputNames":["list"],"outputs":{"item":"input list items one by one as separate events"},"outputNames":["item"],"helpText":"<p>Split input list into separate events. They will be sent out as separate events, one item at a time.</p><p>Each event causes activation of all modules where the output item is sent to.</p>"}')
		}
	}
}
