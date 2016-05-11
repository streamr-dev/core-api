package core

databaseChangeLog = {

	changeSet(author: "eric", id: "map-modules-3") {
		def modules = [
				[
						id      : 500,
						name    : "GetMultiFromMap",
						jsonHelp: '{"params":{},"paramNames":[],"inputs":{"in":"input map"},"inputNames":["in"],"outputs":{"founds":"an array indicating for each output with 0 (false) and (1) whether a value was found","out-1":"a (default) value from map, output name is used as key"},"outputNames":["founds","out-1"],"helpText":"<p>Get multiple values&nbsp;from a Map. Number of outputs is specified via module options (wrench icon).&nbsp;<strong>The names of outputs are used as map keys so make sure to change them!</strong></p>"}',
				],
				[
						id      : 501,
						name    : "BuildMap",
						jsonHelp: '{"params":{},"paramNames":[],"inputs":{"in-1":"default single input, name used as key in Map"},"inputNames":["in-1"],"outputs":{"map":"produced map"},"outputNames":["map"],"helpText":"<p>Build a new Map from given inputs. Number of inputs is specified via module options (wrench icon).&nbsp;<strong>The names of input are used as map keys so make sure to change them!</strong></p>"}',
				],
		]

		modules.eachWithIndex { module, i ->
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
}
