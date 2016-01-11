(function(exports) {

    function StreamrTextField(parent, data) {
        this.parent = $(parent)
        this.value = data && data.textFieldValue ? data.textFieldValue : ""
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

        this.width = this.textArea.outerWidth()
        this.height = this.textArea.outerHeight()

        this.bindEvents()
    }

    StreamrTextField.prototype.bindEvents = function() {
        var _this = this
        this.textArea.keyup(function() {
            _this.value = $(this).val()
        })
        this.sendButton.click(function() {
            _this.sendValue(_this.value)
        })

        this.textArea.mouseup(function(){
            if (  $(this).outerWidth()  != _this.width || $(this).outerHeight() != _this.height ) {
                $(_this).trigger("update")
            }
            _this.width = $(this).outerWidth()
            _this.height = $(this).outerHeight()
        })
    }

    StreamrTextField.prototype.sendValue = function(value) {
        $(this).trigger("input", value)
    }

    StreamrTextField.prototype.toJSON = function() {
        return {
            textFieldValue: this.value
        }
    }

    exports.StreamrTextField = StreamrTextField

})(typeof(exports) !== 'undefined' ? exports : window)