package core

databaseChangeLog = {

	changeSet(author: "eric", id: "map-modules-1") {

		insert(tableName: "module_category") {
			column(name: "id", valueNumeric: 51)
			column(name: "version", valueNumeric: 0)
			column(name: "name", value: "Map")
			column(name: "sort_order", valueNumeric: 141)
			column(name: "module_package_id", valueNumeric: 1)
		}

		def modules = [
		    [
				id: 221,
				name: "CountByKey",
				jsonHelp: '{\\"params\\":{\\"sort\\":\\"Whether key-count pairs should be order by count\\",\\"maxKeyCount\\":\\"Maximum number of (sorted) key-count pairs to keep. Everything else will be dropped.\\"},\\"paramNames\\":[\\"sort\\",\\"maxKeyCount\\"],\\"inputs\\":{\\"key\\":\\"The (string) key\\"},\\"inputNames\\":[\\"key\\"],\\"outputs\\":{\\"map\\":\\"Key-count pairs\\",\\"valueOfCurrentKey\\":\\"The occurrence count of the last key received. \\"},\\"outputNames\\":[\\"map\\",\\"valueOfCurrentKey\\"],\\"helpText\\":\\"<p>Keeps count of the occurrences of keys.</p>\\"}'
			],
			[
				id: 222,
				name: "SumByKey",
				jsonHelp: '{\\"params\\":{\\"windowLength\\":\\"Limit moving window size of sum.\\",\\"sort\\":\\"Whether key-sum pairs should be order by sums\\",\\"maxKeyCount\\":\\"Maximum number of (sorted) key-sum pairs to keep. Everything else will be dropped.\\"},\\"paramNames\\":[\\"windowLength\\",\\"sort\\",\\"maxKeyCount\\"],\\"inputs\\":{\\"value\\":\\"The value to be added to aggregated sum.\\",\\"key\\":\\"The (string) key\\"},\\"inputNames\\":[\\"value\\",\\"key\\"],\\"outputs\\":{\\"map\\":\\"Key-sum pairs\\",\\"valueOfCurrentKey\\":\\"The aggregated sum of the last key received. \\"},\\"outputNames\\":[\\"map\\",\\"valueOfCurrentKey\\"],\\"helpText\\":\\"<p>Keeps aggregated sums of received key-value-pairs by key.</p>\\"}'
			],
			[
			    id: 223,
				name: "ForEach",
				jsonHelp: '{"params":{"canvas":"The \\\\"sub\\\\" canvas that implements the ForEach-loop \\\\"body\\\\""},"paramNames":["canvas"],"inputs":{"key":"Differentiate between canvas"},"inputNames":["key"],"outputs":{"map":"The state of outputs of all distinct Canvases by key."},"outputNames":["map"],"helpText":"<p>This module allows you to reuse a Canvas saved into the Archive as a module in your current Canvas.</p><p>A separate Canvas instance will be created for each distinct key, which enables ForEach-like behavior to be implemented. The canvas instances will also retain state as expected.</p><p>Any parameters, inputs or outputs you export will be visible on the module. You can export endpoints by right-clicking on them and selecting \\\\"Toggle export\\\\".</p>"}'

			]
		]

		modules.each { module ->
			insert(tableName: "module") {
				column(name: "id", valueNumeric: module.id)
				column(name: "version", valueNumeric: 0)
				column(name: "category_id", valueNumeric: 51)
				column(name: "implementing_class", value: "com.unifina.signalpath.map.${module.name}")
				column(name: "name", value: module.name)
				column(name: "js_module", value: "GenericModule")
				column(name: "type", value: "module")
				column(name: "module_package_id", valueNumeric: 1)
				column(name: "json_help", value: module.jsonHelp)
			}
		}
	}

	changeSet(author: "eric", id: "map-modules-2-test", context: "test") {
		insert(tableName: "stream") {
			column(name: "version", valueNumeric: 0)
			column(name: "api_key", value: "mapmodulesspeckey-api-key")
			column(name: "description", value: "Stream for MapModulesSpec functional test")
			column(name: "feed_id", valueNumeric: 7)
			column(name: "name", value: "MapModulesSpec")
			column(name: "config", value: '{"topic":"pltRMd8rCfkij4mlZsQkJB","fields":[{"name":"key","type":"string"},{"name":"value","type":"number"}]}')
			column(name: "user_id", valueNumeric: 1)
			column(name: "uuid", value: "pltRMd8rCfkij4mlZsQkJB")
			column(name: "class", value: "com.unifina.domain.data.Stream")
		}
	}
}
