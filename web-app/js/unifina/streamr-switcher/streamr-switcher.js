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
		this.switcherEl = $('<input type="checkbox" data-class="switcher-lg" class="streamr-switcher-checkbox">')
		this.switcherEl.attr("checked", this.checked)
		this.switcherEl.change(function() {
			if(_this.mustSendValue === true)
				_this.sendValue(_this.getValue())
		})
		this.parent.append(this.switcherEl)
		this.parent.addClass("switcher-container")
		this.switcher = this.createSwitcher(this.switcherEl,{
			theme: 'square',
			on_state_content: "1",
			off_state_content: "0"
		})
	}

	StreamrSwitcher.prototype.getDragCancelAreas = function() {
		return ["div.switcher-inner"]
	}

	StreamrSwitcher.prototype.getValue = function() {
		return this.switcherEl.prop('checked')
	}

	StreamrSwitcher.prototype.toJSON = function() {
		return {
			switcherValue: this.getValue()
		}
	}

	StreamrSwitcher.prototype.updateState = function(state) {
		this.mustSendValue = false
		if(state == true && this.getValue() === false)
			this.switcher.on()
		else if(state == false && this.getValue() === true)
			this.switcher.off()
		this.mustSendValue = true
	}
    //
	StreamrSwitcher.prototype.sendValue = function(value) {
		$(this).trigger("input", value)
	}

	StreamrSwitcher.prototype.receiveResponse = function(p) {
		if (p["switcherValue"] === true || p["switcherValue"] === false)
			this.updateState(p["switcherValue"])
	}

	StreamrSwitcher.prototype.createSwitcher = function($el, options) {
		var _this = this
		var switcher = {}
		var DEFAULTS = {
			theme: null,
			on_state_content: 'ON',
			off_state_content: 'OFF'
		}
		var box_class;
		if (options == null) {
			options = {};
		}
		switcher.options = $.extend({}, DEFAULTS, options);
		switcher.$checkbox = null;
		switcher.$box = null;
		if ($el.is('input[type="checkbox"]')) {
			box_class = $el.attr('data-class');
			switcher.$checkbox = $el;
			switcher.$box = $(
					'<div class="switcher">' +
						'<div class="switcher-toggler"></div>' +
						'<div class="switcher-inner">' +
							'<div class="switcher-state-on">' +
								switcher.options.on_state_content +
							'</div>' +
							'<div class="switcher-state-off">'+
								switcher.options.off_state_content +
							'</div>' +
						'</div>' +
					'</div>'
			);
			if (switcher.options.theme) {
				switcher.$box.addClass('switcher-theme-' + switcher.options.theme);
			}
			if (box_class) {
				switcher.$box.addClass(box_class);
			}
			switcher.$box.insertAfter(switcher.$checkbox).prepend(switcher.$checkbox);
		} else {
			switcher.$box = $el;
			switcher.$checkbox = $('input[type="checkbox"]', switcher.$box);
		}
		if (switcher.$checkbox.prop('disabled')) {
			switcher.$box.addClass('disabled');
		}
		if (switcher.$checkbox.is(':checked')) {
			switcher.$box.addClass('checked');
		}
		switcher.$checkbox.on('click', function(e) {
			return e.stopPropagation();
		});
		switcher.$box.on('touchend click', (function(_this) {
			return function(e) {
				e.stopPropagation();
				e.preventDefault();
				return switcher.toggle();
			};
		})(switcher));

		switcher.toggle = function() {
			if (switcher.$checkbox.click().is(':checked')) {
				return switcher.$box.addClass('checked');
			} else {
				return switcher.$box.removeClass('checked');
			}
		}

		switcher.enable = function() {
			_this.switcher.$checkbox.prop('disabled', false);
			return _this.switcher.$box.removeClass('disabled');
		}

		switcher.disable = function() {
			_this.switcher.$checkbox.prop('disabled', true);
			return _this.switcher.$box.addClass('disabled');
		}

		switcher.on = function() {
			if (!_this.switcher.$checkbox.is(':checked')) {
				_this.switcher.$checkbox.click();
				return _this.switcher.$box.addClass('checked');
			}
		}

		switcher.off = function() {
			if (_this.switcher.$checkbox.is(':checked')) {
				_this.switcher.$checkbox.click();
				return _this.switcher.$box.removeClass('checked');
			}
		}

		return switcher;
	}

exports.StreamrSwitcher = StreamrSwitcher

})(typeof(exports) !== 'undefined' ? exports : window)