
function ConfirmButton(el, options, callback) {
    var _this = this
    this.el = $(el)
    this.options = $.extend({
        title: "&nbsp;",
        message: "Are you sure?"
    }, options)
    this.el.click(function(event) {
        event.preventDefault()
        bootbox.confirm({
            title: _this.options.title,
            message: _this.options.message,
            callback: callback ? callback : function(result) {
                if(result) {
                    _this.el.parent("form").submit()
                }
            }
        })
    })
}