package core
databaseChangeLog = {
	changeSet(author: "jtakalai", id: "20160524-1226-1") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1015)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 27)	// Text
			column(name: "implementing_class", value: "com.unifina.signalpath.text.StringTemplate")
			column(name: "name", value: "StringTemplate")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{' +
				'"params":{"template":"Text template"},' +
				'"paramNames":["template"],' +
				'"inputs":{"args":"Map of arguments that will be substituted into the template"},' +
				'"inputNames":["args"],' +
				'"outputs":{"errors":"List of error strings","result":"Completed template string"},' +
				'"outputNames":["errors", "result"],' +
				'"helpText":"' +
					'<p>For template syntax, see <a href=\'https://github.com/antlr/stringtemplate4/blob/master/doc/cheatsheet.md\' target=\'_blank\'>StringTemplate cheatsheet</a>.</p>' +
					'<p>Values of the <strong>args</strong> map are added as substitutions in the template. For example, incoming map <strong>{name: &quot;Bernie&quot;, age: 50}</strong> substituted into template &quot;<strong>Hi, &lt;name&gt;!</strong>&quot;&nbsp;would produce string &quot;Hi, Bernie!&quot;</p>' +
					'<p>Nested maps can be accessed with dot notation:&nbsp;<strong>{name: &quot;Bernie&quot;, pet: {species: &quot;dog&quot;, age: 3}}</strong>&nbsp;substituted into &quot;<strong>What a cute &lt;pet.species&gt;!</strong>&quot; would result in &quot;What a cute dog!&quot;.</p>' +
					'<p>Lists will be smashed together: <strong>{pals:&nbsp;[&quot;Sam&quot;, &quot;Herb&quot;, &quot;Dud&quot;]}</strong>&nbsp;substituted into &quot;<strong>BFF: me, &lt;pals&gt;</strong>&quot; results in &quot;BFF: me, SamHerbDud&quot;. Separator must be explicitly given: &quot;<strong>BFF: me, &lt;pals; separator=&quot;, &quot;&gt;</strong>&quot; gives &quot;BFF: me, Sam, Herb, Dud&quot;.</p>' +
					'<p>Transforming list items can be done with <em>{ x | f(x) }</em> syntax, e.g. <strong>{pals:&nbsp;[&quot;Sam&quot;, &quot;Herb&quot;, &quot;Dud&quot;]}</strong> substituted into &quot;<strong>&lt;pals: { x | Hey &lt;x&gt;! }&gt; Hey y&#39;all!</strong>&quot; results in &quot;Hey Sam! Hey Herb! Hey Dud! Hey y&#39;all!&quot;.</p>' +
				'"}')
		}
	}

	changeSet(author: "jtakalai", id: "20160525-1713") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 1016)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 27) // Text
			column(name: "implementing_class", value: "com.unifina.signalpath.text.JsonParser")
			column(name: "name", value: "JsonParser")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{' +
				'"params":{},' +
				'"paramNames":[],' +
				'"inputs":{"json":"JSON string to parse"},' +
				'"inputNames":["json"],' +
				'"outputs":{"errors":"List of error strings","result":"Map, List or value that the JSON string represents"},' +
				'"outputNames":["errors", "result"],' +
				'"helpText":"<p>JSON string should fulfill the <a href=\'http://json.org/\' target=\'_blank\'>JSON specification</a>.</p>"' +
			'}')
		}
	}
}
