(function(exports) {

function SignalPathBrowser(options) {
	RemoteTabs.call(this, options)
	this._onSelect = function() {}
}
SignalPathBrowser.prototype = Object.create(RemoteTabs.prototype)
SignalPathBrowser.prototype.onTabShown = function($tabBody) {
	var that = this

	$('.signalpath-browser', $tabBody).on('selected', function(e, id) {
		that.close()
		that._onSelect(id)
	})
}
SignalPathBrowser.prototype.onSelect = function(fn) {
	this._onSelect = fn
	return this
}

SignalPathBrowser.contentAppender = function contentAppender(browser, url) {
	function appendContent() {
		if (browser.data('requesting') || browser.data('complete'))
			return;
		else
			browser.data('requesting', true)
			
		var offset
		var $lastOffsetRow = browser.find(".has-offset:last")
		if ($lastOffsetRow && $lastOffsetRow.data("offset"))
			offset = $lastOffsetRow.data("offset")

		browser.append("<div class='loading fa fa-spin fa-spinner'></div>")

		$.get(url, {
				max: 100,
				offset: (offset ? offset+1 : 0)
			}, function(data) {
				var tbody = browser.find('table tbody')
				var oldLength = tbody.find('tr').length
				tbody.append(data)
				var newLength = tbody.find('tr').length

				if (oldLength === newLength)
					browser.data('complete', true)

				browser.data('requesting', false)
				browser.find('.loading').remove()

				$('tr.selectable', browser).click(function(e) {
					e.stopPropagation()
					e.preventDefault()
					$(browser).trigger('selected', $(this).data('id'))
				})
			}
		)
	}

	browser.scroll(function() {
	    if (browser.scrollTop() >= browser.find('table').height() - browser.height())
		    appendContent()
	})

	appendContent()
}

exports.SignalPathBrowser = SignalPathBrowser

})(window)

