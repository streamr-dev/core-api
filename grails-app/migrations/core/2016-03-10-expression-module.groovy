package core

databaseChangeLog = {

	changeSet(author: "eric", id: "expression-module-1") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 567)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 1)
			column(name: "implementing_class", value: "com.unifina.signalpath.simplemath.Expression")
			column(name: "name", value: "Expression")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{"expression":"mathematical expression to evaluate"},"paramNames":["expression"],"inputs":{"x":"variable for default expression","y":"variable for default expression"},"inputNames":["x","y"],"outputs":{"out":"result if evaluation succeeded","error":"error message if evaluation failed (e.g. syntax error in expression)"},"outputNames":["out","error"],"helpText":"<p>Evaluate arbitrary mathematical expressions containing operators, variables, and functions. Variables introduced in an&nbsp;expression&nbsp;will automatically appear as&nbsp;inputs.</p>\\n\\n<p>&nbsp;</p>\\n\\n<p>See&nbsp;<a href=https://github.com/uklimaschewski/EvalEx#supported-operators>https://github.com/uklimaschewski/EvalEx#supported-operators</a>&nbsp;for further detail about supported features.</p>"}')
		}
	}
}
