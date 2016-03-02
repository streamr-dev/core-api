(function(exports) {

	function StreamrSwitcher(parent, json) {
		this.parent = $(parent)
		this.mustSendValue = true
		if(json)
			this.checked = json.switcherValue
		$(this).on("started", this.startFunction)
	}

	StreamrSwitcher.prototype.render = function() {
		var _this = this
		this.switcher = $('<input type="checkbox" data-class="switcher-lg" class="streamr-switcher-checkbox">')
		this.switcher.attr("checked", this.checked)
		this.switcher.change(function() {
			if(_this.mustSendValue === true)
				_this.sendValue(_this.getValue())
		})
		this.parent.append(this.switcher)
		this.parent.addClass("switcher-container")
		this.switcher.switcher({
			theme: 'square',
			on_state_content: "1",
			off_state_content: "0"
		})
	}

	StreamrSwitcher.prototype.getDragCancelAreas = function() {
		return ["div.switcher-inner"]
	}

	StreamrSwitcher.prototype.getValue = function() {
		return this.switcher.prop('checked')
	}

	StreamrSwitcher.prototype.toJSON = function() {
		return {
			switcherValue: this.getValue()
		}
	}

	StreamrSwitcher.prototype.updateState = function(state) {
		this.mustSendValue = false
		if(state == true && this.getValue() === false)
			this.switcher.switcher("on")
		else if(state == false && this.getValue() === true)
			this.switcher.switcher("off")
		this.mustSendValue = true
	}

	StreamrSwitcher.prototype.sendValue = function(value) {
		$(this).trigger("input", value)
	}

	StreamrSwitcher.prototype.receiveResponse = function(p) {
		if (p["switcherValue"] === true || p["switcherValue"] === false)
			this.updateState(p["switcherValue"])
	}

exports.StreamrSwitcher = StreamrSwitcher

})(typeof(exports) !== 'undefined' ? exports : window)