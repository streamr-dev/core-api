(function(exports) {

function _generateTabId() {
	return 'remote-tabs-tab-' + ('' + Math.floor(Math.random() * 100000))
}

/**
 * Widget to show remote url contents in tabs
 */
function RemoteTabs(options) {
	this._tabs = []

	this._options = $.extend({
		modal: true
	}, options)

	this._onSelect = function() {}
}

RemoteTabs.prototype.tab = function(title, url) {
	this._tabs.push({
		id: _generateTabId(),
		title: title,
		url: url,
		active: this._tabs.length === 0 ? 'active' : ''
	})

	return this
}

RemoteTabs.prototype.modal = function() {
	var that = this

	this.render()

	this._dialog = bootbox.dialog({
		message: this.$el,
		onEscape: function() {
			that.close()
		},
		title: 'Load',
		buttons: {
			'Ok': function() {},
			'Cancel': function() {},
		}
	})

	this._dialog.on('hidden.bs.modal', function() {
		that.$el.empty().remove()
		that._dialog.empty().remove()
	})

	return this
}

RemoteTabs.prototype.close = function() {
	this.$el.empty().remove()

	if (this._dialog)
		this._dialog.modal('hide')
}

RemoteTabs.prototype.render = function() {
	var that = this

	if (this.$el)
		this.close()

	this.$el = $(Mustache.render($('#remote-tabs-template').html(), {
		tabs: this._tabs
	}))

	// when a tab is shown, reload its contents
	$('a[data-toggle="tab"]', this.$el)
		.on('show.bs.tab', function(e) {
			that._tabShow($(e.target))
		})

	this._tabShow($('li.active a', this.$el))

	return this.$el
}

RemoteTabs.prototype.onTabShown = function() {}

RemoteTabs.prototype._tabShow = function($a) {
	var that = this
	var id = $a.data('target')
	var url = $a.data('url')

	var $tabBody = $(id, this.$el)

	// console.log('_tabShow', id, url)

	$.get(url, function(content) {
		$tabBody.html(content)
		that.onTabShown($tabBody)
	})

	return true
}

RemoteTabs.prototype.onSelect = function(fn) {
	this._onSelect = fn
	return this
}

exports.RemoteTabs = RemoteTabs

// ------------------------------------

function SignalPathBrowser() {
	RemoteTabs.call(this)
}
SignalPathBrowser.prototype = Object.create(RemoteTabs.prototype);
SignalPathBrowser.prototype.onTabShown = function($tabBody) {
	var that = this

	$('.signalpath-browser', $tabBody).on('selected', function(e, url) {
		that.close()
		that._onSelect(url)
	})
}

exports.SignalPathBrowser = SignalPathBrowser	

})(window)

