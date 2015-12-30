(function(exports) {

    function StreamrTextField(parent, options) {
        var _this = this

        this.parent = parent
        this.options = options
        this.value = options.value || ""

        this.render()

        this.bindEvents()
    }

    StreamrTextField.prototype.render = function() {
        this.container = $("<div/>", {
            class: "form-inline"
        })
        this.textArea = $("<textarea/>", {
            class: "form-control",
            text: this.value
        })
        this.sendButton = $("<button/>", {
            class: "btn btn-primary send-btn",
            text: "Send"
        })
        this.container.append(this.textArea)
        this.container.append(this.sendButton)
        this.parent.append(this.container)
        this.parent.addClass("text-field")
    }

    StreamrTextField.prototype.bindEvents = function() {
        var _this = this
        this.textArea.keyup(function() {
            _this.value = $(this).val()
        })
        this.sendButton.click(function() {
            _this.sendValue(_this.value)
        })
    }

    StreamrTextField.prototype.sendValue = function(value) {
        $(this).trigger("input", value)
    }

    StreamrTextField.prototype.getData = function() {
        return {
            value: this.value
        }
    }

    exports.StreamrTextField = StreamrTextField

})(typeof(exports) !== 'undefined' ? exports : window)