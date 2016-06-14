package core
databaseChangeLog = {
	changeSet(author: "jtakalai", id: "20160523-1504-2") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1011)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 3)	// Utils
			column(name: "implementing_class", value: "com.unifina.signalpath.utils.ListAsTable")
			column(name: "name", value: "ListAsTable")
			column(name: "js_module", value: "TableModule")
			column(name: "type", value: "module event-table-module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "webcomponent", value: "streamr-table")
			column(name: "json_help", value: '{' +
				'"params":{},' +
				'"paramNames":[],' +
				'"inputs":{"list":"List to be shown"},' +
				'"inputNames":["list"],' +
				'"outputs":{},' +
				'"outputNames":[],' +
				'"helpText":"' +
					'<p>Display contents of a list as a table. If it\'s a list of maps, break maps into columns</p>' +
				'"}')
		}
	}
}
