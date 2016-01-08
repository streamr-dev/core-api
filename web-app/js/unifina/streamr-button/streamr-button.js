(function(exports) {

	function StreamrButton(parent, data, options) {
		this.parent = $(parent)
		this.options = options

		this.createButton()

		if(this.options && this.options.alwaysEnabled)
			this.enable()
	}

	StreamrButton.prototype.createButton = function() {
		var _this = this
		this.button = $("<button/>", {
			class: 'button-module-button btn btn-lg btn-primary btn-block disabled'
		})
		this.parent.append(this.button);
		this.changeName()

		this.button.click(function(e){
			_this.sendValue()
		})
	}

	StreamrButton.prototype.receiveResponse = function(p) {
		if(p["buttonName"] !== undefined)
			this.changeName(p["buttonName"])
	}

	StreamrButton.prototype.sendValue = function() {
		$(this).trigger("input")
	}

	StreamrButton.prototype.changeName = function(name) {
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
		if(!this.alwaysEnabled)
			this.button.addClass("disabled")
	}

exports.StreamrButton = StreamrButton

})(typeof(exports) !== 'undefined' ? exports : window)