package core
databaseChangeLog = {
	changeSet(author: "eric", id: "clock-module-update") {
		update(tableName: "module") {
			column(name: "json_help", value: '{"params":{"format":"Format of the string date","rate":"the rate of the interval","unit":"the unit of the interval"},"paramNames":["format","rate","unit"],"inputs":{},"inputNames":[],"outputs":{"date":"String notation of the time and date","timestamp":"unix timestamp"},"outputNames":["date","timestamp"],"helpText":"<p>Tells the time and date at fixed time intervals (by default every second). Outputs the time either in string notation of given format or as a timestamp (milliseconds from 1970-01-01 00:00:00.000).</p>\\n\\n<p>The time interval can be chosen with parameter&nbsp;<em>unit&nbsp;</em>and&nbsp;granularly controlled via parameter&nbsp;<em>rate</em>. For example,&nbsp;<em>unit=minute&nbsp;</em>and&nbsp;<em>rate=2</em>&nbsp;will tell the time every other minute.</p>"}')
			where("id = 209")
		}
	}
}