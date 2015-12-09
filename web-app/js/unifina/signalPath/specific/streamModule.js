SignalPath.StreamModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.GenericModule(data,canvas,prot)

	var super_getHelp = prot.getHelp

	prot.resultHelpText
	prot.getHelp = function(extended, cb) {
		var _this = this
		super_getHelp(extended, function(html){
			if(!prot.resultHelpText) {
				$.getJSON(Streamr.createLink("stream", "getDataRange", pub.getInput("stream").getValue()), {}, function (dataRange) {
					if (dataRange != null) {
						var beginDate = new Date(dataRange.beginDate)
						var endDate = new Date(dataRange.endDate)

						html += "<p>This stream has data from <b>" +
								$.datepicker.formatDate("dd-mm-yy", beginDate) +
								"</b> to <b>" +
								$.datepicker.formatDate("dd-mm-yy", beginDate) +
								"</b>.</p>"
					} else
						html += "<p>This stream has no history</p>"
					prot.resultHelpText = html
					cb(prot.resultHelpText)
				})
			} else {
				cb(prot.resultHelpText)
			}
		})
	}
	return pub;
}
