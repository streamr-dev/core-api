package core
databaseChangeLog = {

	changeSet(author: "eric", id: "new-variadic-modules-1") {
		sql("UPDATE module SET hide = true, name = 'Add (old)' WHERE id = 100")
		sql("UPDATE module SET hide = true, name = 'PassThrough (old)' WHERE id = 90")
		sql("UPDATE module SET hide = true, name = 'Filter (old)' WHERE id = 181")
		sql("UPDATE module SET hide = true, name = 'GetMultiFromMap (old)' WHERE id = 500")
	}

	changeSet(author: "eric", id: "new-variadic-modules-2") {
		// VariadicAddMulti
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 520)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 1)
			column(name: "implementing_class", value: "com.unifina.signalpath.simplemath.VariadicAddMulti")
			column(name: "name", value: "Add")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"outputNames":["sum"],"inputs":{},"helpText":"<p>Adds together two or more numeric input values.</p>","inputNames":[],"params":{},"outputs":{"sum":"Sum of inputs"},"paramNames":[]}')
			column(name: "alternative_names", value: "Plus")
		}

		// PassThrough
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 521)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 19)
			column(name: "implementing_class", value: "com.unifina.signalpath.utils.VariadicPassThrough")
			column(name: "name", value: "PassThrough")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"outputNames":[],"inputs":{},"helpText":"<p>This module just sends out whatever it receives.</p>","inputNames":[],"params":{},"outputs":{},"paramNames":[]}')
		}

		// Filter
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 522)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 3)
			column(name: "implementing_class", value: "com.unifina.signalpath.utils.VariadicFilter")
			column(name: "name", value: "Filter")
			column(name: "js_module", value: "FilterModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{"pass":"The filter condition. 1 (true) for letting the event pass, 0 (false) to filter it out","in":"The incoming event (any type)"},"inputNames":["pass","in"],"outputs":{"out":"The event that came in, if passed. If filtered, nothing is sent"},"outputNames":["out"],"helpText":"Only lets the incoming value through if the value at <span class=\'highlight\'>pass</span> is 1. If this condition is not met, no event is sent out."}')
			column(name: "alternative_names", value: "Select, Pick, Choose")
		}

		// GetMultiFromMap
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 523)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 51)
			column(name: "implementing_class", value: "com.unifina.signalpath.map.VariadicGetMultiFromMap")
			column(name: "name", value: "GetMultiFromMap")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{"in":"input map"},"inputNames":["in"],"outputs":{"founds":"an array indicating for each output with 0 (false) and (1) whether a value was found","out-1":"a (default) value from map, output name is used as key"},"outputNames":["founds","out-1"],"helpText":"<p>Get multiple values&nbsp;from a Map. &nbsp;<strong>The names of outputs are used as map keys so make sure to change them!</strong></p>"}')
		}
	}
}
