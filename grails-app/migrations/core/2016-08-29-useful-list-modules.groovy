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
			column(name: "implementing_class", value: "com.unifina.signalpath.list.Range")
			column(name: "name", value: "Range")
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

	changeSet(author: "eric", id: "useful-list-modules-5") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 548)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.AddToList")
			column(name: "name", value: "AddToList")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{"index":"index to add to, from 0 to length of list"},"paramNames":["index"],"inputs":{"item":"item to add to list","list":"the list to add to"},"inputNames":["item","list"],"outputs":{"error":"error string if given invalid index","list":"the result if operation successful"},"outputNames":["error","list"],"helpText":"<p>Insert an item into&nbsp;an arbitrary position of a List. Unless adding to the very end of a list,&nbsp;items starting from&nbsp;<em>index </em>are&nbsp;all shifted to the right to allow insertion of new item.</p>\\n"}')
		}
	}

	changeSet(author: "eric", id: "useful-list-modules-6") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 549)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.AppendToList")
			column(name: "name", value: "AppendToList")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{"item":"item to append","list":"list to append to"},"inputNames":["item","list"],"outputs":{"list":"resulting list"},"outputNames":["list"],"helpText":"<p>Append an item to the end of a List.</p>\\n"}')
		}
	}

	changeSet(author: "eric", id: "useful-list-modules-7") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 550)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.BuildList")
			column(name: "name", value: "BuildList")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{},"inputNames":[],"outputs":{},"outputNames":[],"helpText":"<p>Build a fixed-sized list from values at inputs.</p>\\n"}')
		}
	}

	changeSet(author: "eric", id: "useful-list-modules-8") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 551)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.ContainsItem")
			column(name: "name", value: "ContainsItem")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{"item":"item to look for","list":"list to look from"},"inputNames":["item","list"],"outputs":{"found":"true if found; false otherwise"},"outputNames":["found"],"helpText":"<p>Checks whether a list contains an item.</p>\\n"}')
		}
	}

	changeSet(author: "eric", id: "useful-list-modules-9") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 552)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.FlattenList")
			column(name: "name", value: "FlattenList")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{"deep":"whether to flatten recursively"},"paramNames":["deep"],"inputs":{"in":"input list"},"inputNames":["in"],"outputs":{"out":"flattened list"},"outputNames":["out"],"helpText":"<p>Flattens lists inside a list, e.g. [1, [2,3], [4, 5], 6, [7, 8], 9] -&gt; [1, 2, 3, 4, 5, 6, 7, 8, 9].</p>\\n\\n<p>&nbsp;</p>\\n\\n<p>If <em>deep&nbsp;= true</em>, flattening will be done recursively. E.g. [1, [2, [3, [4, 5, [6]]], 7], 8, 9] -&gt;&nbsp;[1, 2, 3, 4, 5, 6, 7, 8, 9]. Otherwise only one level of flattening will be perfomed.</p>\\n"}')
		}
	}

	changeSet(author: "eric", id: "useful-list-modules-10") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 553)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.HeadList")
			column(name: "name", value: "HeadList")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{"limit":"the maximum number of items to include"},"paramNames":["limit"],"inputs":{"in":"input list"},"inputNames":["in"],"outputs":{"out":"a list containing the first items of a list"},"outputNames":["out"],"helpText":"<p>Retrieves the first (a maximum of <em>limit</em>)&nbsp;items of a list.</p>\\n"}')
		}
	}

	changeSet(author: "eric", id: "useful-list-modules-11") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 554)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.MergeList")
			column(name: "name", value: "MergeList")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{"head":"the first items of the merged list","tail":"the last items of the merged list"},"inputNames":["head","tail"],"outputs":{"out":"merged list"},"outputNames":["out"],"helpText":"<p>Merge two lists (<em>head + tail)</em> together to form a singe list. Merging is simply done by adding items of&nbsp;<em>tail&nbsp;</em>to the end of&nbsp;<em>head&nbsp;</em>to form a single list.</p>\\n"}')
		}
	}

	changeSet(author: "eric", id: "useful-list-modules-12") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 555)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.RemoveFromList")
			column(name: "name", value: "RemoveFromList")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{"index":"position to remove item from"},"paramNames":["index"],"inputs":{"in":"list to remove item from"},"inputNames":["in"],"outputs":{"out":"the list with the item removed"},"outputNames":["out"],"helpText":"<p>Remove an item from a list by index. Given an invalid index, this module simply outputs&nbsp;the original&nbsp;input list.</p>\\n"}')
		}
	}

	changeSet(author: "eric", id: "useful-list-modules-13") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 556)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.ReverseList")
			column(name: "name", value: "ReverseList")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{"in":"list"},"inputNames":["in"],"outputs":{"out":"reversed list"},"outputNames":["out"],"helpText":"<p>Reverses a list.</p>\\n"}')
		}
	}

	changeSet(author: "eric", id: "useful-list-modules-14") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 557)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.SortList")
			column(name: "name", value: "SortList")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{"order":"ascending or descending"},"paramNames":["order"],"inputs":{"in":"list to sort"},"inputNames":["in"],"outputs":{"out":"sorted list"},"outputNames":["out"],"helpText":"<p>Sort a list.</p>\\n"}')
		}
	}

	changeSet(author: "eric", id: "useful-list-modules-15") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 558)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.TailList")
			column(name: "name", value: "TailList")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{"limit":"the maximum number of items to include"},"paramNames":["limit"],"inputs":{"in":"input list"},"inputNames":["in"],"outputs":{"out":"a list containing the last items of a list"},"outputNames":["out"],"helpText":"<p><br />\\nRetrieves the last&nbsp;(a maximum of limit) items of a list.</p>\\n"}')
		}
	}

	changeSet(author: "eric", id: "useful-list-modules-16") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 559)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.Unique")
			column(name: "name", value: "Unique")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{"list":"list with possible duplicates"},"inputNames":["list"],"outputs":{"list":"list without duplicates"},"outputNames":["list"],"helpText":"<p>Removes duplicate items from a list resulting in a list of unique items. The first occurrence of an item is kept&nbsp;and subsequent occurrences removed.</p>\\n"}')
		}
	}

	changeSet(author: "eric", id: "useful-list-modules-17") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 560)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.IndexOfItem")
			column(name: "name", value: "IndexOfItem")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{"item":"item to look for","list":"list to look in"},"inputNames":["item","list"],"outputs":{"index":"outputs the index of the first occurrence; does not output anything if no occurrences"},"outputNames":["index"],"helpText":"<p>Finds the index of the first occurrence of an item in a list.</p>\\n"}')
		}
	}

	changeSet(author: "eric", id: "useful-list-modules-18") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 561)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 52)
			column(name: "implementing_class", value: "com.unifina.signalpath.list.IndexesOfItem")
			column(name: "name", value: "IndexesOfItem")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{},"paramNames":[],"inputs":{"item":"item to look for","list":"item to look for"},"inputNames":["item","list"],"outputs":{"indexes":"list of indexes of occurrences; empty list if none"},"outputNames":["indexes"],"helpText":"<p>Finds indexes of all&nbsp;occurrences of an item in a list.</p>\\n"}')
		}
	}
}