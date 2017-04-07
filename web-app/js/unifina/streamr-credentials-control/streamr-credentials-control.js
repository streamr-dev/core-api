
(function(exports) {
    
    var listTemplate = '' +
        '<div class="col-xs-12">' +
            '<table class="auth-key-table col-xs-12">' +
                '<thead>' +
                    '<th class="name-header">' +
                        '<span class="title">Name</span>' +
                    '</th>' +
                    '{[ if (typeof(showPermissions) !== "undefined" && showPermissions) { ]}' +
                        '<th class="permission-header">' +
                            '<span class="title">Permission</span>' +
                        '</th>' +
                    '{[ } ]}' +
                    '<th class="key-header">' +
                        '<span class="title">Key</span>' +
                        '<span class="visibility-controls pull-right">' +
                            '<i class="fa fa-eye show-icon" title="Show keys"/>' +
                            '<i class="fa fa-eye-slash hide-icon" title="Hide keys"/>' +
                        '</span>' +
                    '</th>' +
                    '<th class="action-header"></th>' +
                '</thead>' +
                '<tbody></tbody>' +
            '</table>' +
        '</div>' +
        '<div class="create-auth-key new-auth-key-row col-xs-12"></div>'
    
    var keyTemplate = '' +
        '<td class="name-field">{{name}}</td>' +
        '{[ if (typeof(showPermissions) !== "undefined" && showPermissions) { ]}' +
            '<td class="permission-field">{{permission}}</td>' +
        '{[ } ]}' +
        '<td class="key-field">' +
            '<span class="key-container">' +
               '<span class="key">{{id}}</span>' +
            '</span>' +
        '</td>' +
        '<td class="action-field"> ' +
            '<div class="dropdown pull-right hidden-sm hidden-md hidden-lg">' +
                '<button class="dashboard-menu-toggle dropdown-toggle btn btn-sm" data-toggle="dropdown">' +
                    '<i class="navbar-icon fa fa-caret-down"></i>' +
                '</button>' +
                '<ul class="dropdown-menu pull-right">' +
                    '<li>' +
                        '<button type="button" class="form-group copy-to-clipboard btn btn-block pull-right" title="Copy key to clipboard" data-clipboard-text="{{id}}">' +
                            '<span><i class="fa fa-copy"/> Copy</span>' +
                        '</button>' +
                    '</li>' +
                    '<li>' +
                        '<button type="button" class="form-group auth-key-delete-button btn btn-block btn-danger pull-right" title="Delete key">' +
                            '<span><i class="icon fa fa-trash-o"/> Delete</span>' +
                        '</button>' +
                    '</li>' +
                '</ul>' +
            '</div>' +
            '<div class="hidden-xs">' +
                '<button type="button" class="form-group auth-key-delete-button btn btn-danger pull-right" title="Delete key">' +
                    '<span class="icon fa fa-trash-o"/>' +
                '</button>' +
                '<button type="button" class="form-group copy-to-clipboard btn pull-right" title="Copy key to clipboard" data-clipboard-text="{{id}}">' +
                    '<span class="fa fa-copy"/>' +
                '</button>' +
            '</div>' +
        '</td>'
    
    var inputTemplate = '' +
        '<div class="input-group">' +
            '<input type="text" class="new-auth-key-field form-control" placeholder="Enter name" autofocus="" name="name">' +
            '{[ if (typeof(showPermissions) !== "undefined" && showPermissions) { ]}' +
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
            '{[ } ]}' +
            '<span class="input-group-btn">' +
                '<button class="new-auth-key-button btn btn-default pull-right" type="button">' +
                    '<span class="icon fa fa-plus"></span>' +
                '</button>' +
            '</span>' +
        '</div>'
            
    var CredentialsControl = Backbone.View.extend({
        template: _.template(listTemplate),
        initialize: function(opts) {
            var _this = this
            this.showPermissions = opts.showPermissions
            
            this.streamId = opts.streamId
            this.username = opts.username
            this.url = opts.url

            if (this.username && this.streamId) {
                throw new Error("Cannot give both streamId and username!")
            } else if (!this.username && !this.streamId) {
                throw new Error("Must give either streamId or username!")
            }

            $.getJSON(this.url)
                .then(function(keys) {
                    _this.keys = keys
                    _this.render()
                })
                .fail(function(e) {
                    Streamr.showError(e.message || e.responseJSON.message)
                })
        },
        render: function() {
            var _this = this
            
            this.$el.append(this.template({
                showPermissions: this.showPermissions
            }))
            this.table = this.$el.find("table.auth-key-table")
            this.listEl = this.$el.find(".auth-key-table tbody")
            this.inputEl = this.$el.find(".create-auth-key")
            this.showKeysIcon = this.$el.find("thead .show-icon")
            this.hideKeysIcon = this.$el.find("thead .hide-icon")
            for (var i in this.keys) {
                this.addKey(new Key(this.keys[i]))
            }
            this.input = new CreateAuthInput({
                el: this.inputEl,
                showPermissions: this.showPermissions,
                url: this.url
            })
            this.listenTo(this.input, "new", function(key) {
                _this.addKey(key)
            })
            this.showKeysIcon.click(function() {
                _this.showKeys()
            })
            this.hideKeysIcon.click(function() {
                _this.hideKeys()
            })
        },
        addKey: function(key) {
            this.listEl.append(new KeyInList({
                model: key,
                showPermissions: this.showPermissions,
                url: this.url + '/' + key.get('id')
            }).el)
        },
        showKeys: function() {
            var _this = this
            this.table.addClass("show-keys")
            this.hideKeysTimeout = setTimeout(function() {
                _this.hideKeys()
            }, 20000)
        },
        hideKeys: function() {
            clearTimeout(this.hideKeysTimeout)
            this.table.removeClass("show-keys")
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
        className: 'auth-key-table-row',
        initialize: function(opts) {
            this.showPermissions = opts.showPermissions
            this.url = opts.url
            this.render()
        },
        render: function() {
            var _this = this
            
            this.$el.html(this.template($.extend(this.model.toJSON(), {
                showPermissions: this.showPermissions
            })))
            this.deleteButton = this.$el.find(".auth-key-delete-button")
            new Clipboard(this.$el[0].getElementsByClassName("copy-to-clipboard"))
                .on('success', function(e) {
                    Streamr.showSuccess("Key successfully copied to clipboard!")
                })
                .on('error', function(e) {
                    Streamr.showError("Something went wrong when copying key. Please copy key manually.")
                })
            new ConfirmButton(this.deleteButton, {
                message: 'Do you really want to revoke and delete key <strong>' + this.model.get('name') + '</strong>?'
            }, function(res) {
                if (res) {
                    _this.delete()
                }
            })
        },
        delete: function() {
            var _this = this
            $.ajax(this.url, {
                method: 'DELETE'
            }).then(function() {
                _this.remove()
            }).fail(function(e) {
                Streamr.showError(e.message || e.responseJSON.message)
            })
        }
    })
    
    var CreateAuthInput = Backbone.View.extend({
        template: _.template(inputTemplate),
        events: {
            "click .new-auth-key-button": "createKey",
            "keypress input[name=name]": "testEnterAndCreateKey"
        },
        initialize: function(opts) {
            this.showPermissions = opts.showPermissions
            this.url = opts.url
            
            this.render()
        },
        render: function() {
            var _this = this
            this.$el.append(this.template({
                showPermissions: this.showPermissions
            }))
            this.nameInput = this.$el.find("input[name=name]")
            this.createButton = this.$el.find(".new-auth-key-button")
            
            if (this.showPermissions) {
                this.permissionDropDown = this.$el.find(".permission-dropdown")
                this.permissionLabel = this.permissionDropDown.find(".permission-label")
                this.permissionInput = this.permissionDropDown.find("input[name=permission]")
                this.permissionDropDown.find("li[data-opt]").on("click", function() {
                    var selection = $(this).data("opt")
                    _this.permissionLabel.text("can " + selection)
                    _this.permissionInput.val(selection)
                })
            }
        },
        testEnterAndCreateKey: function(e) {
            if (e.which === 13) {
                this.createKey()
            }
        },
        createKey: function() {
            var _this = this
            
            var name = this.nameInput.val() || ""
            var permission = this.showPermissions ? this.permissionInput.val() : undefined
            $.post(this.url, {
                name: name,
                permission: permission
            }).then(function(data) {
                _this.nameInput.val('')
                
                // To close the suggestion dropdown
                _this.nameInput.blur()
                _this.nameInput.focus()
                
                _this.trigger("new", new Key(data))
            })
        }
    })
    
    exports.StreamrCredentialsControl = CredentialsControl
    
})(typeof(exports) !== 'undefined' ? exports : window)