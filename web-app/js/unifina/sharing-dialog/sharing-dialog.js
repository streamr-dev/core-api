/**
 * Dialog that shows users and their permissions to given resource
 *
 * Can be spawned with something like
 *  <button onclick="sharePopup('${createLink(uri: "/api/v1/streams/" + stream.uuid)}')"> Share </button>
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
        '<div class="form-inline access-row">' +
            '<div class="form-group user-label"><%= user %></div>' +
            '<div class="form-group permission-dropdown btn-group">' +
                '<button type="button" class="btn dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">' +
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
            '<button class="form-group user-delete-button btn btn-danger"><span class="icon fa fa-trash-o"></span></button>' +
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
        '<div class="owner-row">' +
            '<span>Owner: </span>' +
            '<span class="owner-label"><%= owner %></span>' +
        '</div>' +
        '<div class="access-list"></div>' +
        '<div class="form-inline new-user-row">' +
            '<input type="text" class="new-user-field form-group" placeholder="Enter username" autofocus>' +
            '<button class="new-user-button btn btn-default" type="button"><span class="icon fa fa-plus"></span></button>' +
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
                    sharePopup.cancelChanges()
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
                sharePopup.saveChanges()
            }
        },

        initialize: function(args) {
            this.$el.html(accessListTemplate({
                owner: args.owner
            }))
            this.$ownerLabel = this.$(".owner-label")
            this.$accessList = this.$(".access-list")
            this.$newUserField = this.$(".new-user-field")

            this.listenTo(accessList, 'add', this.createAccessView)
            this.listenTo(accessList, 'reset', this.addAll)
        },

        accessViews: [],
        createAccessView: function(accessRow) {
            var view = new AccessView({model: accessRow})
            this.$accessList.append(view.render().$el)
            this.accessViews.push(view)
        },

        addAll: function() {
            //this.$accessList.html()
            accessList.each(this.createAccessView, this)
        }
    })

    // de-pluralizes the collection name, e.g. "canvases" -> "canvas"
    // TODO: use the .name property somehow?
    function urlToResourceName(url) {
        var resAndId = url.split("/").slice(-2)
        var res = resAndId[0]
        var id = resAndId[1]
        res = res.slice(0, res.endsWith("ses") ? -2 : -1)
        return res + " " + id
    }

    var sharingDialog;
    var listView;
    var resourceUrl;
    var originalPermissions = {};   // { username: { operation: Permission object } }
    var originalOwner;

    /** Entry point */
    exports.sharePopup = function(url, resourceName) {
        if (!!sharingDialog) { throw "Cannot open sharePopup, already open!" }
        resourceUrl = url

        // get current permissions
        var originalPermissionList = []
        $.getJSON(resourceUrl + "/permissions").success(function(data) {
            originalOwner = _.chain(data)
                .filter(function(p) { return p.operation === "share" && !p.id })
                .pluck("user")
                .first().value()
            originalPermissionList = _(data).filter(function(p) { return !!p.id })
        }).always(function() {
            sharingDialog = bootbox.dialog({
                title: "Set permissions for <span class='resource-name-label'></span>",
                message: "Loading...",
                backdrop: true,     // The backdrop is displayed, and clicking on it dismisses the dialog
                //onEscape: true,
                buttons: {
                    cancel: {
                        label: "Cancel",
                        callback: sharePopup.cancelChanges
                    },
                    save: {
                        label: "Save",
                        className: "btn-primary",
                        callback: sharePopup.saveChanges
                    }
                }
            })
            $(".resource-name-label").text(resourceName || urlToResourceName(resourceUrl))

            listView = new AccessListView({
                el: ".modal-body",
                id: "access-list",
                owner: originalOwner
            })

            // map list of Permissions to list of Access rows on the form (combine)
            originalPermissions = {}
            var initialAccessMap = {}
            _(originalPermissionList).each(function(p) {
                if (!p || !p.user || !p.operation) { return }
                if (!originalPermissions[p.user]) {
                    originalPermissions[p.user] = {}
                    initialAccessMap[p.user] = { user: p.user }
                }
                originalPermissions[p.user][p.operation] = p
                initialAccessMap[p.user][p.operation] = true
            })
            accessList.reset(_.values(initialAccessMap))
        })
    }

    exports.sharePopup.saveChanges = function() {
        if (!sharingDialog) { throw "Cannot close sharePopup, try opening it first!" }

        _(listView.accessViews).each(function(view) {
            view.$(".user-access-row").removeClass("has-error")
        })

        // find changes
        var addedPermissions = []
        var removedPermissions = []
        var testedUsers = {}
        accessList.each(function(accessModel) {
            var user = accessModel.attributes.user
            var before = originalPermissions[user] || {}
            var after = accessModel.attributes
            if (before.read  && !after.read)  { removedPermissions.push(before.read.id) }
            if (before.write && !after.write) { removedPermissions.push(before.write.id) }
            if (before.share && !after.share) { removedPermissions.push(before.share.id) }
            if (!before.share && after.share) { addedPermissions.push({user: user, operation: "share"}) }
            if (!before.write && after.write) { addedPermissions.push({user: user, operation: "write"}) }
            if (!before.read  && after.read)  { addedPermissions.push({user: user, operation: "read"}) }
            testedUsers[user] = true;
        })
        // completely removed users don't show up in accessList, need to be tested separately
        _(originalPermissions).each(function (before, user) {
            if (user in testedUsers) { return }    // continue
            if (before.read)  { removedPermissions.push(before.read.id) }
            if (before.write) { removedPermissions.push(before.write.id) }
            if (before.share) { removedPermissions.push(before.share.id) }
        })

        if (addedPermissions.length == 0 && removedPermissions.length == 0) {
            listView.remove()
            return true;                // close modal
        }

        // generate one API call for each change
        var started = 0
        var successful = 0
        var failed = 0
        var changedUsers = {}
        var errorMessages = []
        _(addedPermissions).each(function(permission) {
            started += 1
            $.ajax({
                url: resourceUrl + "/permissions",
                method: "POST",
                data: JSON.stringify(permission),
                contentType: "application/json",
                success: onSuccess,
                error: onError
            })
        })
        _(removedPermissions).each(function(id) {
            started += 1
            $.ajax({
                url: resourceUrl + "/permissions/" + id,
                method: "DELETE",
                contentType: "application/json",
                success: onSuccess,
                error: onError
            })
        })
        function onSuccess(response) {
            successful += 1
            changedUsers[response.user] = true
            if (successful + failed === started) { onDone() }
        }
        function onError(e) {
            failed += 1
            errorMessages.push(e.responseJSON.error)
            if (e.responseJSON.fault === "user") {
                // find Access row with bad username and highlight
                _(listView.accessViews).each(function(view) {
                    if (view.model.get("user") === e.responseJSON.user) {
                        view.$(".user-access-row").addClass("has-error")
                    }
                })
            }
            if (successful + failed === started) { onDone() }
        }
        function onDone() {
            if (successful === started) {
                Streamr.showSuccess("Changed sharing to " + _.keys(changedUsers).join(", "))
                listView.remove()
                sharingDialog.modal("hide")     // close after successful save
                sharingDialog = null
            } else {
                Streamr.showError("Errors during saving:\n * " + errorMessages.join("\n * "))
            }
        }

        return false;                           // don't close the dialog yet, saving is still in progress...
    }

    exports.sharePopup.cancelChanges = function() {
        if (!sharingDialog) { throw "Cannot close sharePopup, try opening it first!" }
        listView.remove()
        sharingDialog = null
    }

})(window)