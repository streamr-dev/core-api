
var StreamrNameEditor = Backbone.View.extend({
    
    initialize: function(options) {
        var _this = this
        
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
    },

    render: function() {
        var _this = this
        
        this.$name = $("<span class='name'/>")
        this.$input = $("<input/>", {
            type: 'text',
            class: 'form-control name-input'
        })
        
        this.$editor = $("<div/>", {
            class: 'form-inline'
        }).append(this.$input)
        
        this.$el.on('click', function() {
            _this.openEditor()
        })
        
        this.$input.on('change', function() {
            _this.setName($(this).val())
        })
        
        this.$input.on('keyup', function(e) {
            _this.keyPressEventHandler(e)
        })

        this.$el.append(this.$name)
        this.$el.append(this.$editor)
    },

    update: function(updateObject) {
        this.name = typeof updateObject !== 'undefined' && updateObject.name || this.name
        this.$name.text(this.name)
        this.closeEditor()
        this.$editor.find('input').val(this.name)
    },

    setName: function(name, opt) {
        this.update({
            name: name
        })
        if (!opt || !opt.silent) {
            this.trigger("changed", this.name)
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
            this.setName(this.$editor.find('input').val())
        // Escape
        } else if (e.keyCode == 27) {
            e.preventDefault()
            this.$editor.find('input').val(this.name)
            this.closeEditor()
        }
    },

    getName: function() {
        return this.name
    }

})
