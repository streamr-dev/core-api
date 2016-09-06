package core
databaseChangeLog = {

	changeSet(author: "eric", id: "new-moving-average-module-1") {
		sql("UPDATE module SET hide = true, name = 'MovingAverage (old)' WHERE id = 2")
	}

	changeSet(author: "eric", id: "new-moving-average-module-2") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 524)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 2)
			column(name: "implementing_class", value: "com.unifina.signalpath.filtering.MovingAverageModule")
			column(name: "name", value: "MovingAverage")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"outputNames":["out"],"inputs":{"in":"Input values"},"helpText":"<p>This module calculates the simple moving average (MA, SMA) of values arriving at the input. Each value is assigned equal weight. The moving average is calculated based on a sliding window of adjustable length.</p>","inputNames":["in"],"params":{"minSamples":"Minimum number of input values received before a value is output","length":"Length of the sliding window, ie. the number of most recent input values to include in calculation"},"outputs":{"out":"The moving average"},"paramNames":["length","minSamples"]}')
			column(name: "alternative_names", value: "SMA")
		}
	}
}
