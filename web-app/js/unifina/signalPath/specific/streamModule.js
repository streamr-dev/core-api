SignalPath.StreamModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.GenericModule(data,canvas,prot)

	var super_getHelp = prot.getHelp

	prot.getHelp = function(extended, cb) {
		super_getHelp(extended, function(html) {
			if(pub.getInput("stream").getValue() != null) {
				$.ajax({
					url: Streamr.createLink({uri: "api/v1/streams/"+pub.getInput("stream").getValue()+"/range"}),
					type: 'GET',
					dataType: 'json',
					global: false, // if no access to stream itself, error 40x gets returned. prevent global 40x handlers from firing
					success: function (dataRange) {
						if (dataRange.beginDate && dataRange.endDate) {
							var beginDate = new Date(dataRange.beginDate)
							var endDate = new Date(dataRange.endDate)

							html += "<p>This stream has data from <b>" +
								$.datepicker.formatDate("yy-mm-dd", beginDate) +
								"</b> to <b>" +
								$.datepicker.formatDate("yy-mm-dd", endDate) +
								"</b>.</p>"
						}

						cb(html)
					},
					error: function() {
						cb(html)
					}
				})
			} else {
				cb(html)
			}
		})
	}
	return pub;
}
