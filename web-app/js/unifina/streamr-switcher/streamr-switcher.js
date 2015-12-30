(function(exports) {

	function StreamrSwitcher(parent, options) {
		this.parent = parent
		this.options = options
		this.checked = options.value

		this.sendOutput = false

		this.createSwitcher()
	}

	StreamrSwitcher.prototype.createSwitcher = function() {
		var _this = this
		this.switcher = $('<input type="checkbox" data-class="switcher-lg" class="streamr-switcher-checkbox">')
		this.switcher.attr("checked", this.checked)
		this.switcher.change(function(){
			//if(_this.sendOutput)
				_this.sendValue(_this.getValue())
		})
		this.parent.append(this.switcher)
		this.parent.addClass("switcher-container")
		this.switcher.switcher({
			theme: 'square',
			on_state_content: "1",
			off_state_content: "0"
		})

		$(SignalPath).on("started", function(){
			_this.sendOutput = true
		})

		$(SignalPath).on("stopped", function(){
			_this.sendOutput = false
		})
	}

	StreamrSwitcher.prototype.getValue = function() {
		return this.switcher.prop('checked')
	}

	StreamrSwitcher.prototype.getData = function() {
		return {
			value: this.getValue
		}
	}

	StreamrSwitcher.prototype.sendValue = function(value) {
		$(this).trigger("input", value)
	}

	StreamrSwitcher.prototype.receiveResponse = function(p) {

	}

exports.StreamrSwitcher = StreamrSwitcher

})(typeof(exports) !== 'undefined' ? exports : window)