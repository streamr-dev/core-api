package core
databaseChangeLog = {
	changeSet(author: "eric", id: "list-modules-1") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 539)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.ForEachItem")
			column(name: "name", value: "ForEachItem")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{"keepState":"when false, sub-canvas state is cleared after lists have been processed  ","canvas":"the sub-canvas to be executed"},"paramNames":["keepState","canvas"],"inputs":{},"inputNames":[],"outputs":{"numOfItems":"indicates how many times the sub-canvas was executed"},"outputNames":["numOfItems"],"helpText":"<p>Execute a sub-canvas for each item of input lists.</p>\\n\\n<p>&nbsp;</p>\\n\\n<p>The&nbsp;exported inputs and outputs of sub-canvas <em>canvas</em>&nbsp;appear as list inputs and list outputs. The input lists are iterated element-wise, and the sub-canvas is executed every time a value is available for each input list. If input list sizes vary, the sub-canvas is executed as many times as the&nbsp;smallest list is of size. After the input lists have been iterated through,&nbsp;and the sub-canvas activated accordingly, lists of produced values are sent to output lists.</p>\\n\\n<p>&nbsp;</p>\\n\\n<p>The output&nbsp;<em>numOfItems</em>&nbsp;indicates how many times the sub-canvas was executed, i.e., the size of the smallest input list.</p>\\n\\n<p>&nbsp;</p>\\n\\n<p>You may want to look into the module&nbsp;<strong>RepeatItem</strong>&nbsp;when using this module to repeat parameter values etc.</p>"}')
		}
	}

	changeSet(author: "eric", id: "list-modules-2") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 540)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.RepeatItem")
			column(name: "name", value: "RepeatItem")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{"times":"times to repeat the item"},"paramNames":["times"],"inputs":{"item":"item to be repeated"},"inputNames":["item"],"outputs":{"list":"the produced list"},"outputNames":["list"],"helpText":"<p>Make a list out of an&nbsp;item by repeating it <em>times&nbsp;</em>times.&nbsp;</p>"}')
		}
	}
}