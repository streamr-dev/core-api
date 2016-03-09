package core
databaseChangeLog = {

	changeSet(author: "eric", id: "serialized-feed-to-blob-1") {
		def startingOffset = 224

		def modules = [
			[
				name: "ContainsValue",
			],
			[
				name: "GetFromMap",
			],
			[
				name: "HeadMap",
			],
			[
				name: "KeysToList",
			],
			[
				name: "PutToMap",
			],
			[
				name: "SortMap",
			],
			[
				name: "TailMap",
			],
			[
				name: "ValuesToList",
			],
		]

		modules.eachWithIndex { module, i ->
			insert(tableName: "module") {
				column(name: "id", valueNumeric: 224 + i)
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
}
