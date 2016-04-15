package core

databaseChangeLog = {

	changeSet(author: "eric", id: "serialized-feed-to-blob-1") {
		def startingOffset = 224

		def modules = [
				[
						name    : "ContainsValue",
						jsonHelp: '{"params":{},"paramNames":[],"inputs":{"in":"a map","value":"a value"},"inputNames":["in","value"],"outputs":{"found":"1.0 if found, else 0.0."},"outputNames":["found"],"helpText":"<p>Determine whether a map contains a value.</p>"}',
				],
				[
						name    : "GetFromMap",
						jsonHelp: '{"params":{"key":"a key"},"paramNames":["key"],"inputs":{"in":"a map"},"inputNames":["in"],"outputs":{"found":"1.0 if key was present in map, 0.0 otherwise.","out":"the corresponding value if key was found."},"outputNames":["found","out"],"helpText":"<p>Retrieve a value from a map by key.</p>"}',
				],
				[
						name    : "HeadMap",
						jsonHelp: '{"params":{"limit":"the number of entries to fetch"},"paramNames":["limit"],"inputs":{"in":"a map"},"inputNames":["in"],"outputs":{"out":"a submap of the first entries of map"},"outputNames":["out"],"helpText":"<p>Retrieve&nbsp;first (n=limit)&nbsp;entries of a map.</p>"}',
				],
				[
						name    : "KeysToList",
						jsonHelp: '{"params":{"limit":"the number of entries to fetch"},"paramNames":["limit"],"inputs":{"in":"a map"},"inputNames":["in"],"outputs":{"out":"a submap of the first entries of map"},"outputNames":["out"],"helpText":"<p>Retrieve&nbsp;first (n=limit)&nbsp;entries of a map.</p>"}',
				],
				[
						name    : "PutToMap",
						jsonHelp: '{"params":{},"paramNames":[],"inputs":{"key":"key to insert","map":"a map","value":"value to insert"},"inputNames":["key","map","value"],"outputs":{"map":"a map with the key-value entry inserted"},"outputNames":["map"],"helpText":"<p>Put a key-value-entry&nbsp;into a map.</p>"}',
				],
				[
						name    : "SortMap",
						jsonHelp: '{"params":{"byValue":"when false (default), sorts by key. when true, sorts by value"},"paramNames":["byValue"],"inputs":{"in":"a map"},"inputNames":["in"],"outputs":{"out":"a sorted map"},"outputNames":["out"],"helpText":"<p>Sorts a map.</p>"}',
				],
				[
						name    : "TailMap",
						jsonHelp: '{"params":{"limit":"the number of entries to fetch"},"paramNames":["limit"],"inputs":{"in":"a map"},"inputNames":["in"],"outputs":{"out":"a submap of the last entries of map"},"outputNames":["out"],"helpText":"<p>Retrieve&nbsp;last (n=limit)&nbsp;entries of a map.</p>"}',
				],
				[
						name    : "ValuesToList",
						jsonHelp: '{"params":{},"paramNames":[],"inputs":{"in":"a map"},"inputNames":["in"],"outputs":{"keys":"values as a list"},"outputNames":["keys"],"helpText":"<p>Retrieves the values of a map.</p>"}',
				],
				[
						name    : "NewMap",
						jsonHelp: '{"params":{"alwaysNew":"When false (defult), same map is sent every time. When true, a new map is sent on each activation."},"paramNames":["alwaysNew"],"inputs":{"trigger":"used to activate module"},"inputNames":["trigger"],"outputs":{"out":"a map"},"outputNames":["out"],"helpText":"<p>Emit a map everytime trigger receives a value.</p>"}',
				],
				[
						name    : "MergeMap",
						jsonHelp: '{"params":{},"paramNames":[],"inputs":{"leftMap":"a map to merge onto","rightMap":"a map to be merged"},"inputNames":["leftMap","rightMap"],"outputs":{"out":"the resulting merged map"},"outputNames":["out"],"helpText":"<p>Merge&nbsp;<strong>rightMap</strong>&nbsp;onto&nbsp;<strong>leftMap</strong>&nbsp;resulting in a single map. In case of conflicting keys,&nbsp;entries of&nbsp;<strong>rightMap</strong>&nbsp;will replace those of <strong>leftMap</strong>.</p>"}',
				],
				[
						name    : "RemoveFromMap",
						jsonHelp: '{"params":{},"paramNames":[],"inputs":{"in":"a map","key":"a key"},"inputNames":["in","key"],"outputs":{"out":"a map without the removed key"},"outputNames":["out"],"helpText":"<p>Remove an entry for a map by key.</p>"}',
				],
				[
						name    : "MapSize",
						jsonHelp: '{"params":{},"paramNames":[],"inputs":{"in":"a map"},"inputNames":["in"],"outputs":{"size":"the number of entries"},"outputNames":["size"],"helpText":"<p>Determine the number of entries in a map.</p>"}',
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

		def mapAsTableJsonHelp = '{"params":{},"paramNames":[],"inputs":{"map":"a map"},"inputNames":["map"],"outputs":{},"outputNames":[],"helpText":"<p>Display the entries of a map as a table.</p>"}'

		insert(tableName: "module") {
			column(name: "id", valueNumeric: startingOffset + modules.size())
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 3)
			column(name: "implementing_class", value: "com.unifina.signalpath.utils.MapAsTable")
			column(name: "name", value: "MapAsTable")
			column(name: "js_module", value: "TableModule")
			column(name: "type", value: "module event-table-module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: mapAsTableJsonHelp)
			column(name: "webcomponent", value: "streamr-table")
		}
	}
}
