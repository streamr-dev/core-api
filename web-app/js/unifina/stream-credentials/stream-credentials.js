
(function(exports) {
    
    var listTemplate = '' +
        '<div class="col-xs-12 title-label">' +
            '<label>Auth keys</label>' +
        '</div>' +
        '<div class="col-xs-12">' +
            '<table class="stream-auth-keys col-xs-12">' +
                '<thead>' +
                    '<th>Name</th>' +
                    '<th>Permission</th>' +
                    '<th>Key</th>' +
                    '<th></th>' +
                '</thead>' +
                '<tbody></tbody>' +
            '</table>' +
        '</div>' +
        '<div class="create-auth-key new-auth-key-row col-xs-12"></div>'
    
    var keyTemplate = '' +
        '<td class="name-label">{{name}}</td>' +
        '<td>read</td>' +
        '<td>{{id}}</td>' +
        '<td> ' +
            '<button type="button" class="form-group auth-key-delete-button btn btn-danger pull-right" title="Delete key">' +
                '<span class="icon fa fa-trash-o"></span>' +
            '</button>' +
            '<button type="button" class="form-group copy-to-clipboard btn pull-right" title="Copy to clipboard" data-clipboard-text="{{id}}">' +
                '<span class="fa fa-copy"></span>' +
            '</button>' +
        '</td>'
    
    var inputTemplate = '' +
        '<div class="input-group">' +
            '<input type="text" class="new-auth-key-field form-control" placeholder="Enter name" autofocus="" name="name">' +
            '<span class="input-group-btn permission-dropdown">' +
                '<button type="button" class="btn btn-default dropdown-toggle permission-dropdown-toggle pull-right" data-toggle="dropdown">' +
                    '<span class="permission-label">can read</span> <span class="caret"></span>' +
                '</button>' +
                '<ul class="permission-dropdown-menu dropdown-menu">' +
                    '<li data-opt="read"><a href="#">can read</a></li>' +
                    '<li data-opt="write"><a href="#">can write</a></li>' +
                '</ul>' +
                '<input type="hidden" name="permission" value="read">' +
            '</span>' +
            '<span class="input-group-btn">' +
                '<button class="new-auth-key-button btn btn-default pull-right" type="button">' +
                    '<span class="icon fa fa-plus"></span>' +
                '</button>' +
            '</span>' +
        '</div>'
            
    var StreamCredentialList = Backbone.View.extend({
        template: _.template(listTemplate),
        initialize: function(opts) {
            this.keys = opts.keys
            this.streamId = opts.streamId
            this.render()
        },
        render: function() {
            var _this = this
            
            this.$el.append(this.template())
            this.listEl = this.$el.find(".stream-auth-keys tbody")
            this.inputEl = this.$el.find(".create-auth-key")
            for (var i in this.keys) {
                this.addKey(new Key(this.keys[i]))
            }
            this.input = new CreateAuthInput({
                el: this.inputEl,
                streamId: this.streamId
            })
            this.listenTo(this.input, "new", function(key) {
                _this.addKey(key)
            })
        },
        addKey: function(key) {
            this.listEl.append(new KeyInList({
                model: key
            }).el)
        }
    })
    
    var Key = Backbone.Model.extend({
        defaults: {
            id: undefined,
            name: '',
            displayName: ''
        }
    })
    
    var KeyInList = Backbone.View.extend({
        template: _.template(keyTemplate),
        tagName: 'tr',
        className: 'auth-key-row',
        initialize: function() {
            this.render()
        },
        render: function() {
            var _this = this
            
            this.$el.html(this.template(this.model.toJSON()))
            this.deleteButton = this.$el.find(".auth-key-delete-button")
            this.clipboardEl = this.$el.find(".copy-to-clipboard")
            new Clipboard(this.clipboardEl[0])
                .on('success', function(e) {
                    Streamr.showSuccess("Id successfully copied to clipboard!")
                })
            new ConfirmButton(this.deleteButton, {
                message: 'Do you really want to revoke and delete auth key ' + this.model.get('name') + '?'
            }, function(res) {
                if (res) {
                    _this.delete()
                }
            })
        },
        delete: function() {
            var _this = this
            var url = Streamr.createLink({
                uri: 'api/v1/keys/' + this.model.get("id")
            })
            $.ajax(url, {
                method: 'DELETE'
            }).then(function() {
                _this.remove()
            }).catch(function(e) {
                Streamr.showError(e.message)
            })
        }
    })
    
    var CreateAuthInput = Backbone.View.extend({
        template: _.template(inputTemplate),
        events: {
          "click .new-auth-key-button": "createKey"
        },
        initialize: function(opts) {
            this.streamId = opts.streamId
            this.render()
        },
        render: function() {
            var _this = this
            this.$el.append(this.template())
            this.nameInput = this.$el.find("input[name=name]")
            this.createButton = this.$el.find(".new-auth-key-button")
            this.permissionDropDown = this.$el.find(".permission-dropdown")
            this.permissionLabel = this.permissionDropDown.find(".permission-label")
            this.permissionInput = this.permissionDropDown.find("input[name=permission]")
            this.permissionDropDown.find("li[data-opt]").on("click", function() {
                var selection = $(this).data("opt")
                _this.permissionLabel.text("can " + selection)
                _this.permissionInput.val(selection)
            })
        },
        createKey: function() {
            var _this = this
            
            var name = this.nameInput.val() || ""
            var url = Streamr.createLink({
                uri: 'api/v1/keys/'
            })
            var permission = this.permissionInput.val()
            $.post(url, {
                name: name,
                streamId: this.streamId,
                permission: permission
            }).then(function(data) {
                _this.trigger("new", new Key(data))
            })
        }
    })
    
    exports.StreamrStreamCredentials = StreamCredentialList
    
})(typeof(exports) !== 'undefined' ? exports : window)