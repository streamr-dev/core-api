
var StreamrNameEditor = Backbone.View.extend({

    events: {
        "click": "openEditor",
        "change input" : "changeName",
        "blur input" : "changeName",
        "keyup input" : "keyPressEventHandler"
    },

    initialize: function(options) {
        var _this = this
        
        this.beforeChange = options.beforeChange

        if (!this.el)
            throw "Element (el) is required!"

        this.name = options.name
        this.opener = options.opener

        if (this.opener) {
            this.opener.click(function() {
                _this.openEditor()
            })
        }

        this.render()
        this.update()
    },

    render: function() {
        this.$name = $("<span class='name'/>")
        this.$editor = $("" +
            "<div class='form-inline'>" +
                "<input type='text' class='form-control name-input'>" +
            "</div>")

        this.$el.append(this.$name)
        this.$el.append(this.$editor)
    },

    update: function() {
        this.$name.text(this.name)
        this.closeEditor()
        this.$editor.find('input').val(this.name)
    },

    changeName: function() {
        var _this = this
        this.name = this.$editor.find('input').val()
        if (this.beforeChange) {
            if (this.beforeChange.length > 1) {
                this.beforeChange(this.name, ready)
            } else {
                if (this.beforeChange(this.name)) {
                    ready()
                }
            }
        } else {
            ready()
        }
        
        function ready() {
            _this.trigger("changed", this.name)
            _this.update()
        }
    },

    openEditor: function() {
        this.$el.addClass('editing')
        this.$editor.find('input').select()
    },

    closeEditor: function() {
        this.$el.removeClass('editing')
    },

    keyPressEventHandler: function (e) {
        // Enter
        if (event.keyCode == 13){
            e.preventDefault()
            this.changeName()
        // Escape
        } else if (e.keyCode == 27) {
            e.preventDefault()
            this.$editor.find('input').val(this.name)
            this.closeEditor()
        }
    },

    getName: function() {
        return this.name
    },

    setName: function(name) {
        this.name = name
        this.update()
    }

})
