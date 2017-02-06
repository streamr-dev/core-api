package core
databaseChangeLog = {
	changeSet(author: "eric", id: "moving-window-module") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 570)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 3) // Utils
			column(name: "implementing_class", value: "com.unifina.signalpath.utils.MovingWindow")
			column(name: "name", value: "MovingWindow")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{"windowLength":"Length of the sliding window, ie. the number of most recent input values to include in calculation","windowType":"behavior of window","minSamples":"Minimum number of input values received before a value is output"},"paramNames":["windowLength","windowType","minSamples"],"inputs":{"in":"values of any type"},"inputNames":["in"],"outputs":{"list":"the window\'s current state as a list"},"outputNames":["list"],"helpText":"<p>Provides&nbsp;a moving window (list)&nbsp;for any types of values. Window size and behavior&nbsp;can be set via parameters.</p>"}')
		}
	}
}