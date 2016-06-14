package core
databaseChangeLog = {
	changeSet(author: "eric", id: "collect-from-maps-module") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 526)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 51)
			column(name: "implementing_class", value: "com.unifina.signalpath.map.CollectFromMaps")
			column(name: "name", value: "CollectFromMaps")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{"selector":"a map property name"},"paramNames":["selector"],"inputs":{"listOrMap":"list or map to collect from"},"inputNames":["listOrMap"],"outputs":{"listOrMap":"collected list or map"},"outputNames":["listOrMap"],"helpText":"<p>Given a list/map of maps, selects from each an entry according to parameter&nbsp;<em>selector,&nbsp;</em>and then returns a list/map of the collected entry values.</p>\\n\\n<p>&nbsp;</p>\\n\\n<p>In case a map does not have an entry for <em>selector,&nbsp;</em>or the value is null, that entry will be simply skipped in the resulting output.</p>\\n\\n<p>&nbsp;</p>\\n\\n<p>Map entry <em>selector</em> supports dot and array notation for selecting from nested maps and lists, e.g. &quot;parents[1].name&quot; would return [&quot;Homer&quot;, &quot;Fred&quot;] for input [{name: &quot;Bart&quot;, parents: [{name: &quot;Marge&quot;}, {name: &quot;Homer&quot;}]}, {name: &quot;Pebbles&quot;, parents: [{name: &quot;Wilma}, {name: &quot;Fred&quot;}]}]</p>"}')
		}
	}
}