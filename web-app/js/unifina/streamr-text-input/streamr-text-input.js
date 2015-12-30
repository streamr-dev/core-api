(function(exports) {

    function StreamrTextInput(parent, options) {
        var _this = this

        this.parent = parent
        this.options = options
        
        this.container = $("<div/>", {
            class: "form-inline"
        })
        this.textArea = $("<textarea/>", {
            class: "form-control",
            style: "height:31px;"
        })
        this.sendButton = $("<button/>", {
            class: "btn btn-primary",
            text: "Send"
        })
        this.container.append(this.textArea)
        this.container.append(this.sendButton)
        this.parent.append(this.container)
    }

    exports.StreamrTextInput = StreamrTextInput

})(typeof(exports) !== 'undefined' ? exports : window)