var Switcher;

Switcher = function($el, options) {
    var box_class;
    if (options == null) {
        options = {};
    }
    this.options = $.extend({}, Switcher.DEFAULTS, options);
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

Switcher.prototype.enable = function() {
    this.$checkbox.prop('disabled', false);
    return this.$box.removeClass('disabled');
};


/*
 * Disable switcher.
 *
 */

Switcher.prototype.disable = function() {
    this.$checkbox.prop('disabled', true);
    return this.$box.addClass('disabled');
};


/*
 * Set switcher to true.
 *
 */

Switcher.prototype.on = function() {
    if (!this.$checkbox.is(':checked')) {
        this.$checkbox.click();
        return this.$box.addClass('checked');
    }
};


/*
 * Set switcher to false.
 *
 */

Switcher.prototype.off = function() {
    if (this.$checkbox.is(':checked')) {
        this.$checkbox.click();
        return this.$box.removeClass('checked');
    }
};


/*
 * Toggle switcher.
 *
 */

Switcher.prototype.toggle = function() {
    if (this.$checkbox.click().is(':checked')) {
        return this.$box.addClass('checked');
    } else {
        return this.$box.removeClass('checked');
    }
};

Switcher.DEFAULTS = {
    theme: null,
    on_state_content: 'ON',
    off_state_content: 'OFF'
};

$.fn.switcher = function(options, attrs) {
    return $(this).each(function() {
        var $this, sw;
        $this = $(this);
        sw = $.data(this, 'Switcher');
        if (!sw) {
            return $.data(this, 'Switcher', new Switcher($this, options));
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

$.fn.switcher.Constructor = Switcher;