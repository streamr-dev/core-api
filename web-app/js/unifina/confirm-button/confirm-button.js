
function ConfirmButton(button, options, callback) {
    var _this = this
    this.button = $(button)
    this.options = $.extend({
        title: "&nbsp;",
        message: "Are you sure?"
    }, options)
    this.button.click(function(event) {
        event.preventDefault()
        bootbox.confirm({
            title: _this.options.title,
            message: _this.options.message,
            callback: callback ? callback : function(result) {
                if(result) {
                    _this.button.parent("form").submit()
                }
            }
        })
    })
}