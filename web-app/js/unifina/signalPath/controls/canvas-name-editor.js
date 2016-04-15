var CanvasNameEditor = Backbone.View.extend({

    events: {
        "click": "openEditor",
        "change input" : "changeName",
        "blur input" : "changeName",
        "click button": "changeName"
    },

    initialize: function(options) {
        var _this = this

        if (!this.el)
            throw "Element (el) is required!"
        if (!options.signalPath)
            throw "options.signalPath is required!"

        this.signalPath = options.signalPath

        $(this.signalPath).on('saved loaded new', function() {
            _this.update()
        })

        this.render()
        this.update()
    },

    render: function() {
        this.$name = $("<span class='canvas-name'/>")
        this.$editor = $("<div class='form-inline'><input type='text' class='form-control input-sm'><button class='btn btn-primary btn-sm'><i class='fa fa-check'></i></button></div>")

        this.$el.append(this.$name)
        this.$el.append(this.$editor)
    },

    update: function() {
        this.$name.text(this.signalPath.getName())
        this.closeEditor()
        this.$editor.find('input').val(this.signalPath.getName())
    },

    changeName: function() {
        this.signalPath.setName(this.$editor.find('input').val())
        this.update()
    },

    openEditor: function() {
        this.$el.addClass('editing')
        this.$editor.find('input').select()
    },

    closeEditor: function() {
        this.$el.removeClass('editing')
    }

})
