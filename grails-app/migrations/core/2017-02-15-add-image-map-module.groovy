package core
databaseChangeLog = {
	changeSet(author: "eric", id: "update-map-module") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 583)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 13) // Visualizations
			column(name: "implementing_class", value: "com.unifina.signalpath.charts.ImageMapModule")
			column(name: "name", value: "ImageMap")
			column(name: "js_module", value: "ImageMapModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: null)
		}
	}
}