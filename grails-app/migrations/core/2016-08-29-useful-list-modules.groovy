package core
databaseChangeLog = {
	changeSet(author: "eric", id: "useful-list-modules-1") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 544)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.ListSize")
			column(name: "name", value: "ListSize")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{"in":"input list"},"inputNames":["in"],"outputs":{"size":"number of items in list"},"outputNames":["size"],"helpText":"<p>Determine size of list.</p>"}')
		}
	}

	changeSet(author: "eric", id: "useful-list-modules-2") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 545)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.Sequence")
			column(name: "name", value: "Sequence")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{"from":"start of sequence; included in sequence.","step":"step size to add/subtract; sign is ignored; an empty sequence is produced if set to 0","to":"upper bound of sequence; not necessarily included in sequence"},"paramNames":["from","step","to"],"inputs":{},"inputNames":[],"outputs":{"out":"the generated sequence"},"outputNames":["out"],"helpText":"<p>Generates a sequence&nbsp;of numbers increasing/decreasing according to a specified <em>step</em>.</p>\\n\\n<p>&nbsp;</p>\\n\\n<p>When&nbsp;<em>from &lt; to</em>&nbsp;a growing sequence is produced.&nbsp;Otherwise (<em>from &gt; to)</em>&nbsp;a decreasing sequence is produced. The sign of parameter&nbsp;<em>step</em>&nbsp;is ignored, and&nbsp;is automatically determined&nbsp;by the inequality relation between&nbsp;<em>from&nbsp;</em>and&nbsp;<em>to</em>.</p>\\n\\n<p>&nbsp;</p>\\n\\n<p>Parameter&nbsp;<em>to</em>&nbsp;acts as an upper bound which means that if sequence generation goes over&nbsp;<em>to</em>, the exceeding values are not included in the sequence. E.g., from=1, to=2, seq=0.3 results in [1, 1.3, 1.6, 1.9], with&nbsp;2.1 notably not included.</p>"}')
		}
	}

	changeSet(author: "eric", id: "useful-list-modules-3") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 546)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.SubList")
			column(name: "name", value: "SubList")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{"from":"start position (included)","to":"end position (not included)"},"paramNames":["from","to"],"inputs":{"in":"input list"},"inputNames":["in"],"outputs":{"error":"error string in case error occurred","out":"extracted sub list if successful"},"outputNames":["error","out"],"helpText":"<p>Extract a sub&nbsp;list from a list.</p>\\n\\n<p>&nbsp;</p>\\n\\n<p>This&nbsp;module is strict&nbsp;about correct indexing. If given incorrect indices, instead of a sub list being produced,&nbsp;an error will be produced in output <em>error</em>.&nbsp;</p>"}')
		}
	}

	changeSet(author: "eric", id: "useful-list-modules-4") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 547)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.ListToMap")
			column(name: "name", value: "ListToMap")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{},"inputNames":[],"outputs":{},"outputNames":[],"helpText":"<p>Turn a list into a map, with&nbsp;list items as values and indices as keys.</p>"}')
		}
	}
}