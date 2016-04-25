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
		this.switcher.streamrSwitcherWidget({
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


	// Copy pasted from pixelAdmin /javascripts/build/plugins_switcher.js
	var StreamrSwitcherWidget = function($el, options) {
		var box_class;
		if (options == null) {
			options = {};
		}
		this.options = $.extend({}, StreamrSwitcherWidget.DEFAULTS, options);
		this.$checkbox = null;
		this.$box = null;
		if ($el.is('input[type="checkbox"]')) {
			box_class = $el.attr('data-class');
			this.$checkbox = $el;
			this.$box = $('<div class="switcher"><div class="switcher-toggler"></div><div class="switcher-inner"><div class="switcher-state-on">' + this.options.on_state_content + '</div><div class="switcher-state-off">' + this.options.off_state_content + '</div></div></div>');
			if (this.options.theme) {
				this.$box.addClass('switcher-theme-' + this.options.theme);
			}
			if (box_class) {
				this.$box.addClass(box_class);
			}
			this.$box.insertAfter(this.$checkbox).prepend(this.$checkbox);
		} else {
			this.$box = $el;
			this.$checkbox = $('input[type="checkbox"]', this.$box);
		}
		if (this.$checkbox.prop('disabled')) {
			this.$box.addClass('disabled');
		}
		if (this.$checkbox.is(':checked')) {
			this.$box.addClass('checked');
		}
		this.$checkbox.on('click', function(e) {
			return e.stopPropagation();
		});
		this.$box.on('touchend click', (function(_this) {
			return function(e) {
				e.stopPropagation();
				e.preventDefault();
				return _this.toggle();
			};
		})(this));
		return this;
	};


	/*
	 * Enable switcher.
	 *
	 */

	StreamrSwitcherWidget.prototype.enable = function() {
		this.$checkbox.prop('disabled', false);
		return this.$box.removeClass('disabled');
	};


	/*
	 * Disable switcher.
	 *
	 */

	StreamrSwitcherWidget.prototype.disable = function() {
		this.$checkbox.prop('disabled', true);
		return this.$box.addClass('disabled');
	};


	/*
	 * Set switcher to true.
	 *
	 */

	StreamrSwitcherWidget.prototype.on = function() {
		if (!this.$checkbox.is(':checked')) {
			this.$checkbox.click();
			return this.$box.addClass('checked');
		}
	};


	/*
	 * Set switcher to false.
	 *
	 */

	StreamrSwitcherWidget.prototype.off = function() {
		if (this.$checkbox.is(':checked')) {
			this.$checkbox.click();
			return this.$box.removeClass('checked');
		}
	};


	/*
	 * Toggle switcher.
	 *
	 */

	StreamrSwitcherWidget.prototype.toggle = function() {
		if (this.$checkbox.click().is(':checked')) {
			return this.$box.addClass('checked');
		} else {
			return this.$box.removeClass('checked');
		}
	};

	StreamrSwitcherWidget.DEFAULTS = {
		theme: null,
		on_state_content: 'ON',
		off_state_content: 'OFF'
	};

	$.fn.streamrSwitcherWidget = function(options, attrs) {
		return $(this).each(function() {
			var $this, sw;
			$this = $(this);
			sw = $.data(this, 'Switcher');
			if (!sw) {
				return $.data(this, 'Switcher', new StreamrSwitcherWidget($this, options));
			} else if (options === 'enable') {
				return sw.enable();
			} else if (options === 'disable') {
				return sw.disable();
			} else if (options === 'on') {
				return sw.on();
			} else if (options === 'off') {
				return sw.off();
			} else if (options === 'toggle') {
				return sw.toggle();
			}
		});
	};

	$.fn.streamrSwitcherWidget.Constructor = StreamrSwitcherWidget;

exports.StreamrSwitcher = StreamrSwitcher

})(typeof(exports) !== 'undefined' ? exports : window)