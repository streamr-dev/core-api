package core
databaseChangeLog = {
	changeSet(author: "eric", id: "export-csv-module") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 571)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 3) // Utils
			column(name: "implementing_class", value: "com.unifina.signalpath.utils.ExportCSV")
			column(name: "name", value: "ExportCSV")
			column(name: "js_module", value: "ExportCSVModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: null)
		}
	}
}