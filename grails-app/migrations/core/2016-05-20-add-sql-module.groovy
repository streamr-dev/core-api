package core
databaseChangeLog = {
	changeSet(author: "jtakalai", id: "20160520-1406-1") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1010)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 1000)		// Integrations
			column(name: "implementing_class", value: "com.unifina.signalpath.remote.Sql")
			column(name: "name", value: "SQL")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{' +
				'"params":{"engine":"Database engine, e.g. MySQL","host":"Database server to connect","database":"Name of the database","username":"Login username","password":"Login password"},' +
				'"paramNames":["engine","host","database","username","password"],' +
				'"inputs":{"sql":"SQL command to be executed"},' +
				'"inputNames":["sql"],' +
				'"outputs":{"errors":"List of error strings","result":"List of rows returned by the database"},' +
				'"outputNames":["errors","result"],' +
				'"helpText":"' +
					'<p>The result is a list of map objects, e.g. <i>[{&quot;id&quot;:0, &quot;name&quot;:&quot;Me&quot;}, {&quot;id&quot;:1, &quot;name&quot;:&quot;You&quot;}]</i></p>' +
				'"}')
		}
	}
}
