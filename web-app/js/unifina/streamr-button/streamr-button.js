(function(exports) {

	function StreamrButton(parent, options) {
		this.parent = parent
		this.options = options

		this.createButton()
	}

	StreamrButton.prototype.createButton = function() {
		var _this = this
		this.button = $("<button/>", {
			class: 'button-module-button btn btn-lg btn-primary btn-block disabled'
		})
		this.parent.append(this.button);
		this.changeName()

		this.button.click(function(e){
			console.log("ButtonValue: "+_this.value)
		})

		$(SignalPath).on("started", function() {
			_this.enable()
		})

		$(SignalPath).on("stopped", function() {
			_this.disable()
		})
	}

	StreamrButton.prototype.receiveResponse = function(p) {
		if(p["buttonName"] !== undefined)
			this.changeName(p["buttonName"])
		if(p["value"] !== undefined) {
			this.changeValue(p["value"])
		}
	}

	StreamrButton.prototype.changeName = function(name) {
		if(!name)
			name = "button"
		this.name = name
		this.button.text(name)
		$(this).trigger("nameChange")
	}

	StreamrButton.prototype.changeValue = function(value) {
		this.value = value
		$(this).trigger("valueChange")
	}

	StreamrButton.prototype.enable = function() {
		this.button.removeClass("disabled")
	}

	StreamrButton.prototype.disable = function() {
		this.button.addClass("disabled")
	}

exports.StreamrButton = StreamrButton

})(typeof(exports) !== 'undefined' ? exports : window)