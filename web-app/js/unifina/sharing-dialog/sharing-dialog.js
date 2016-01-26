/**
 * Dialog that shows users and their permissions to given resource
 *
 * Can be spawned with something like
 *  <button onclick="sharePopup('${createLink(uri: "/api/v1/stream/" + stream.uuid)}')"> Share </button>
 *
 * @create_date 2016-01-21
 */

(function(exports) {
    var KEY_ENTER = 13;
    var KEY_ESC = 27;

    Backbone.sync = function(method, model, options) {
        console.log("Sync request: " + method + ", " + model + ", " + options)
        return undefined
    }

    /** Access by a user to a resource is defined by 0..3 Permissions */
    var Access = Backbone.Model.extend({
        defaults: {
            user: "",
            read: false,
            write: false,
            share: false
        },

        /** maps user-visible options to the 0..3 Permissions */
        options: {
            none: { description: "has no access", permissions: "" },
            read: { description: "can read", permissions: "r" },
            write: { description: "can edit", permissions: "rw" },
            share: { description: "can share", permissions: "rws" },
        },

        setAccess: function(opt) {
            var selected = this.options[opt]
            if (!selected) { throw "Bad access selection: " + opt }
            this.set("read", selected.permissions.indexOf("r") > -1)
            this.set("write", selected.permissions.indexOf("w") > -1)
            this.set("share", selected.permissions.indexOf("s") > -1)
        },

        getAccessDescription: function() {
            return  this.get("share") ? this.options.share.description :
                    this.get("write") ? this.options.write.description :
                    this.get("read")  ? this.options.read.description :
                                        this.options.none.description
        }
    })

    var accessTemplate = _.template(
        '<button class="btn btn-danger user-delete-button">Delete</button>' +
        '<div class="input-group user-access-row">' +
            '<div class="user-label form-control"><%= user %></div>' +
            '<div class="input-group-btn">' +
                '<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">' +
                    '<span class="access-description"><%= state %></span> <span class="caret"></span>' +
                '</button>' +
                '<ul class="dropdown-menu">' +
                    '<li data-opt="read"><a href="#">make read-only</a></li>' +
                    '<li data-opt="write"><a href="#">make editable</a></li>' +
                    '<li data-opt="share"><a href="#">make shareable</a></li>' +
                    '<li role="separator" class="divider"></li>' +
                    '<li><a href="#">give ownership</a></li>' +
                '</ul>' +
            '</div>' +
        '</div>'
    )

    /** Access is shown as a row: username, access description, delete */
    var AccessView = Backbone.View.extend({
        events: {
            "click .delete-button": function() { this.model.destroy() },
        },

        initialize: function() {
            var self = this

            this.$el.html(accessTemplate({
                user: "",
                state: self.model.options.read.description
            }))
            this.$userLabel = this.$(".user-label")
            this.$accessDescription = this.$(".access-description")

            this.$(".user-delete-button").on("click", function() {
                self.model.destroy()
            })

            this.$("li[data-opt]").on("click", function(e) {
                var selection = e.currentTarget.dataset.opt
                self.model.setAccess(selection)
            });

            this.listenTo(self.model, 'change', self.render)
            this.listenTo(self.model, 'destroy', self.remove)
        },

        render: function() {
            this.$userLabel.text(this.model.get("user"))
            this.$accessDescription.text(this.model.getAccessDescription())
            return this
        },
    })

    /** Contents of the sharing popup dialog: list of Access rows */
    var AccessList = Backbone.Collection.extend({
        model: Access
    })
    var accessList = new AccessList()

    var accessListTemplate = _.template(
        '<div class="input-group">' +
            '<span class="input-group-addon">Owner</span>' +
            '<span class="form-control owner-label"><%= owner %></span>' +
        '</div>' +
        '<div class="access-list"></div>' +
        '<div class="input-group">' +
            '<input type="text" class="new-user-field form-control" placeholder="Enter username" autofocus>' +
            '<span class="input-group-btn">' +
                '<button class="new-user-button btn btn-success" type="button">Add</button>' +
            '</span>' +
        '</div>'
    )

    var AccessListView = Backbone.View.extend({
        events: {
            "click .new-user-button": "finishUserInput",
            "keypress .new-user-field": "keyHandler"
        },
        keyHandler: function(e) {
            if (e.which === KEY_ENTER) {
                this.finishUserInput()
            } else if (e.which === KEY_ESC) {
                if (this.$newUserField.val()) {
                    this.$newUserField.val("")
                } else {
                    // TODO: close dialog ("Cancel") on ESC
                }
            }
        },
        finishUserInput: function() {
            var newUser = this.$newUserField.val()
            if (newUser) {
                //TODO: check that user exists(?)
                accessList.create({
                    user: newUser,
                    read: true,
                })
                this.$newUserField.val("")
            } else {
                // TODO: close & save dialog ("Done") on ENTER if username input is empty
            }
        },

        initialize: function(args) {
            this.$el.html(accessListTemplate({
                owner: "Omistelija"
            }))
            this.$ownerLabel = this.$(".owner-label")
            this.$accessList = this.$(".access-list")
            this.$newUserField = this.$(".new-user-field")

            this.listenTo(accessList, 'add', this.createAccessView)
            this.listenTo(accessList, 'reset', this.addAll)
        },

        createAccessView: function(accessRow) {
            var view = new AccessView({model: accessRow})
            this.$accessList.append(view.render().$el)
        },

        addAll: function() {
            //this.$accessList.html()
            accessList.each(this.createAccessView, this)
        }
    })

    /** Entry point */
    function openSharingDialog(resourceUrl) {
        var sharingDialog = bootbox.dialog({
            title: "Set permissions for <b>" + resourceUrl.split("/").slice(-2).join(" ") + "</b>",
            message: "<div id='access-list-div'>Loading...</div>",
            size: 'large',
            buttons: {
                cancel: {
                    label: "Cancel",
                    className: "btn-danger",
                    callback: function() {
                        listView.remove()
                    }
                },
                save: {
                    label: "Save",
                    className: "btn-success",
                    callback: function() {
                        listView.remove()
                        //TODO: save for realz
                        Streamr.showSuccess("Successfully threw away your hard work.")
                    }
                }
            }
        })

        var listView = new AccessListView({
            el: "#access-list-div",
            id: "access-list",
        })
    }

    exports.sharePopup = openSharingDialog

})(window)