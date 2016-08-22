package core
databaseChangeLog = {
	changeSet(author: "jtakalai", id: "20160822-1438-1") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1012)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)	// List
			column(name: "implementing_class", value: "com.unifina.signalpath.list.GetFromList")
			column(name: "name", value: "GetFromList")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{' +
				'"params":{"index":"Index in the list for the item to be fetched. Negative index counts from end of list."},' +
				'"paramNames":["index"],' +
				'"inputs":{"in":"List to be indexed"},' +
				'"inputNames":["in"],' +
				'"outputs":{"out":"Item found at given index","error":"Error message, e.g. <i>List is empty</i>"},' +
				'"outputNames":["out","error"],' +
				'"helpText":"' +
				'<p>Fetch item from a list by index.</p>' +
				'<p>Indexing starts from zero, so the first item has index 0, second has index 1 etc.</p>' +
				'<p>Negative index counts from end of list, so that last item in the list has index -1, second-to-last has index -2 etc.</p>' +
				'"}')
		}
	}
}
