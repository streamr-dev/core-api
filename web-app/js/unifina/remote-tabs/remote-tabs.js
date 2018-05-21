(function(exports) {

function _generateTabId() {
	return 'remote-tabs-tab-' + ('' + Math.floor(Math.random() * 100000))
}

/**
 * Widget to show remote url contents in tabs
 */
function RemoteTabs() {
	this._tabs = []
	this._options = {
		title: 'Load',
		reloadTabContentWhenShown: false
	}
}

RemoteTabs.prototype.title = function(title) {
	this._options.title = title
	return this
}

RemoteTabs.prototype.reloadTabContentWhenShown = function(doReload) {
	this._options.reloadTabContentWhenShown = doReload
	return this
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

RemoteTabs.prototype.show = function(name) {
	// Find the index of the tab
	var i;
	for (i=0;i<this._tabs.length;i++) {
		if (this._tabs[i].title===name)
			break;
	}

	if (i<this._tabs.length) {
		$(this.$el.find("li a")[i]).tab("show")
	}
}

RemoteTabs.prototype.modal = function() {
	var that = this

	this.render()

	this._dialog = bootbox.dialog({
		animate: false,
		message: this.$el,
		onEscape: function() {
			that.close()
		},
		title: this._options.title,
		buttons: {
			'Close': function() {}
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

	if ($tabBody.data('_tabShown') && !this._options.reloadTabContentWhenShown)
		return;

	$tabBody.data('_tabShown', true)

	$.get(url, function(content) {
		$tabBody.html(content)
		that.onTabShown($tabBody)
	})

	return true
}

exports.RemoteTabs = RemoteTabs

})(window)

