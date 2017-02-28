package core
databaseChangeLog = {
    changeSet(author: "eric", id: "add-javascript-module") {
    insert(tableName: "module") {
        column(name: "id", valueNumeric: 584)
        column(name: "version", valueNumeric: 0)
        column(name: "category_id", valueNumeric: 18) // Custom modules
        column(name: "implementing_class", value: "com.unifina.signalpath.custom.JavaScriptModule")
        column(name: "name", value: "JavaScript")
        column(name: "js_module", value: "CustomModule")
        column(name: "type", value: "module")
        column(name: "module_package_id", valueNumeric: 1)
        column(name: "json_help", value: '{"params":{"function":"the function to be invoked"},"paramNames":["function"],"inputs":{"arguments":"arguments to the chosen function as a list"},"inputNames":["arguments"],"outputs":{"return":"the value returned by the function if not null"},"outputNames":["return"],"helpText":"<p>Allows execution of JavaScript code. Click the <strong>Edit code</strong> button to open the code editor.</p>\\n\\n<p>&nbsp;</p>\\n\\n<p>Identifiers declared in the global scope of the provided JavaScript code will be analzyed. Functions declared in global scope will become invocable through parameter <em>function</em>&nbsp;(drop-down menu). Variables declared in global scope will be re-assignable via appearing module inputs.&nbsp;</p>\\n\\n<p>&nbsp;</p>\\n\\n<p>For example, the code:</p>\\n\\n<blockquote>\\n<p>var a = 512;<br />\\nvar b = &quot;hello&quot;;<br />\\n<br />\\nfunction plus(param1, param2) {<br />\\n&nbsp; &nbsp; return a * param1 + param2;<br />\\n}<br />\\n<br />\\nfunction report() {<br />\\n&nbsp; &nbsp; system.debug(b);<br />\\n}</p>\\n</blockquote>\\n\\n<p>would appear as a module with two functions to choose from (plus or report) and two variables to assign to (a&nbsp;and b).</p>\\n\\n<p>&nbsp;</p>\\n\\n<p>To interact with Streamr, a special object named&nbsp;<strong>system</strong>&nbsp;is accessable in the global scope of JavaScript context. It provides the following facilities:</p>\\n\\n<table align=\\"center\\" border=\\"1\\" cellpadding=\\"2\\" cellspacing=\\"1\\" style=\\"width: 500px;\\">\\n\\t<caption>Functions provided by <strong>system</strong></caption>\\n\\t<thead>\\n\\t\\t<tr>\\n\\t\\t\\t<th scope=\\"col\\">function</th>\\n\\t\\t\\t<th scope=\\"col\\">arguments</th>\\n\\t\\t\\t<th scope=\\"col\\">return</th>\\n\\t\\t\\t<th scope=\\"col\\">purpose</th>\\n\\t\\t</tr>\\n\\t</thead>\\n\\t<tbody>\\n\\t\\t<tr>\\n\\t\\t\\t<td>system.debug</td>\\n\\t\\t\\t<td>object</td>\\n\\t\\t\\t<td>-</td>\\n\\t\\t\\t<td>Logging debug messages</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td>system.time</td>\\n\\t\\t\\t<td>-</td>\\n\\t\\t\\t<td>date</td>\\n\\t\\t\\t<td>Current datetime on Canvas</td>\\n\\t\\t</tr>\\n\\t</tbody>\\n</table>\\n\\n<p>&nbsp;</p>\\n\\n<p>See the User Guide for more information on programmable modules.</p>"}')
    }
}
}