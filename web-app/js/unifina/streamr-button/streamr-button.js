(function(exports) {

	function StreamrButton(parent, data, options) {
		this.parent = $(parent)
		this.options = options
		this.data = data

		if(this.options && this.options.alwaysEnabled) {
			this.alwaysEnabled = this.options.alwaysEnabled
		}
	}

	StreamrButton.prototype.render = function() {
		var _this = this
		var c = 'button-module-button btn btn-lg btn-primary btn-block ' + (_this.alwaysEnabled ? "" : 'disabled')
		this.button = $("<button/>", {
			class: c
		})
		this.parent.append(this.button);
		this.setName(this.getButtonNameFromData())

		this.button.click(function(e){
			_this.sendValue()
		})
	}

	StreamrButton.prototype.updateState = function(state) {
		this.setName(state)
	}

	StreamrButton.prototype.getButtonNameFromData = function(data) {
		var name = undefined
		if(!data)
			data = this.data
		if(data.params)
			data.params.forEach(function(param) {
				if(param.name === "buttonName") {
					this.name = param.value
					name = param.value
				}
			})
		return name
	}

	StreamrButton.prototype.receiveResponse = function(p) {
		if(p["buttonName"] !== undefined)
			this.setName(p["buttonName"])
	}

	StreamrButton.prototype.sendValue = function() {
		$(this).trigger("input")
	}

	StreamrButton.prototype.setName = function(name) {
		if(!name)
			name = "button"
		this.name = name
		this.button.text(name)
		$(this).trigger("update")
	}

	StreamrButton.prototype.enable = function() {
		this.button.removeClass("disabled")
	}

	StreamrButton.prototype.disable = function() {
		if(this.alwaysEnabled === undefined || this.alwaysEnabled === false) {
			this.button.addClass("disabled")
		}
	}

exports.StreamrButton = StreamrButton

})(typeof(exports) !== 'undefined' ? exports : window)