(function(exports) {

	function StreamrButton(parent, data, options) {
		this.parent = $(parent)
		this.options = options
		this.data = data

		this.createButton()

		if(this.options && this.options.alwaysEnabled) {
			this.alwaysEnabled = this.options.alwaysEnabled
			this.enable()
		}
	}

	StreamrButton.prototype.createButton = function() {
		var _this = this
		this.button = $("<button/>", {
			class: 'button-module-button btn btn-lg btn-primary btn-block disabled'
		})
		this.parent.append(this.button);
		this.setName(this.getButtonNameFromData())

		this.button.click(function(e){
			_this.sendValue()
		})
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