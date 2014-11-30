jstz.getTimezoneList = function() {
	var timezoneList = []
	for (var key in jstz.olson.timezones) {
		if (jstz.olson.timezones.hasOwnProperty(key)) {
			timezoneList.push(jstz.olson.timezones[key][0])
		}
	}
	for (var key in jstz.olson.dst_start_dates) {
		if (jstz.olson.dst_start_dates.hasOwnProperty(key) && timezoneList.indexOf(key)<0) {
			timezoneList.push(key)
		}
	}
	timezoneList.sort()
	return timezoneList
}
