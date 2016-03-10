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
			[
			    name: "NewMap",
			],
			[
			    name: "MergeMap",
			]
		]

		modules.eachWithIndex { module, i ->
			insert(tableName: "module") {
				column(name: "id", valueNumeric: startingOffset + i)
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

		insert(tableName: "module") {
			column(name: "id", valueNumeric: startingOffset + modules.size())
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 3)
			column(name: "implementing_class", value: "com.unifina.signalpath.utils.MapAsTable")
			column(name: "name", value: "MapAsTable")
			column(name: "js_module", value: "TableModule")
			column(name: "type", value: "module event-table-module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: null)
			column(name: "webcomponent", value: "streamr-table")
		}
	}
}
