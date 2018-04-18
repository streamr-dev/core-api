/**
 * Dialog that shows users and their permissions to given resource
 *
 * Can be spawned with something like
 *  <button onclick="sharePopup('${createLink(uri: "/api/v1/streams/" + stream.id)}', 'Stream ${stream.name}')"> Share </button>
 *
 * TODO: Split into parts: one file for each Backbone object, one file for the non-Backbone part (exports)
 *
 * @create_date 2016-01-21
 */

(function(exports) {
    var KEY_ENTER = 13;
    var KEY_ESC = 27;
    var emailRegex = /^[^ ]*@[^ ]*\.[^ ]*$/   // something@something.something, no spaces in between

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
        },

        // ignore Backbone sync, handle manually after user presses "Save"
        sync: function(method, model, options) {}
    })

    var accessTemplate = _.template(
        '<div class="access-row col-xs-12">' +
            '<span class="user-label col-xs-6">{{ user }}</span>' +
            '<div class="col-xs-6 access-button-row">' +
                '<button class="form-group user-delete-button btn btn-danger pull-right">' +
                    '<span class="icon fa fa-trash-o"></span>' +
                '</button>' +
                '<button type="button" class="btn btn-default dropdown-toggle permission-dropdown-toggle pull-right" data-toggle="dropdown">' +
                    '<span class="state">{{ state }}</span> <span class="caret"></span>' +
                '</button>' +
                '<ul class="permission-dropdown-menu dropdown-menu">' +
                    '<li data-opt="read"><a href="#">make read-only</a></li>' +
                    '<li data-opt="write"><a href="#">make editable</a></li>' +
                    '<li data-opt="share"><a href="#">make shareable</a></li>' +
                '</ul>' +
            '</div>'+
        '</div>'
    )

    /** Access is shown as a row: username, access description, delete */
    var AccessView = Backbone.View.extend({
        events: {
            "click .delete-button": function() { this.model.destroy() },
        },

        initialize: function() {
            var self = this

            this.$el = $(accessTemplate({
                user: "",
                state: self.model.options.read.description
            }))
            this.el = this.$el[0]
            this.$userLabel = this.$(".user-label")
            this.$stateLabel = this.$el.find(".state")

            this.$el.find(".user-delete-button").on("click", function() {
                self.model.destroy()
            })

            this.$el.find("li[data-opt]").on("click", function(e) {
                var selection = e.currentTarget.dataset.opt
                self.model.setAccess(selection)
                self.$stateLabel.text(self.model.getAccessDescription())
            });

            this.listenTo(self.model, 'change', self.render)
            this.listenTo(self.model, 'destroy', self.remove)
        },

        render: function() {
            this.$userLabel.text(this.model.get("user"))
            this.$stateLabel.text(this.model.getAccessDescription())
            return this
        },
    })

    /** Contents of the sharing popup dialog: list of Access rows */
    var AccessList = Backbone.Collection.extend({
        model: Access
    })
    var accessList = new AccessList()

    var accessListTemplate = _.template(
        '<div class="row">' +
            '<div class="owner-row col-xs-12">' +
                '<div class="col-xs-5 col-sm-5 access-list-caption">' +
                    'Access rights:' +
                '</div>' +
                '<div class="col-xs-7 col-sm-7">' +
                    '<div class="pull-right switcher-container">' +
                        '<input type="checkbox" class="anonymous-switcher pull-right" {{ checked ? "checked" : "" }} >' +
                    '</div>' +
                    '<div class="publish-label pull-right"> Public read access </div>' +
                '</div>' +
            '</div>' +
            '<div class="access-list col-xs-12"></div>' +
            '<div class="new-user-row col-xs-12">' +
                '<div class="input-group">' +
                    '<input type="text" class="new-user-field form-control" placeholder="Enter email address" autofocus>' +
                    '<span class="input-group-btn">' +
                        '<button class="new-user-button btn btn-default pull-right" type="button"><span class="icon fa fa-plus"></span></button>' +
                    '</span>' +
                '</div>' +
            '</div>' +
        '</div>'
    )

    var AccessListView = Backbone.View.extend({
        initialize: function(args) {
            this.$el.html(accessListTemplate({
                checked: !!originalAnonPermission
            }))
            this.$accessList = this.$(".access-list")
            this.$newUserField = this.$(".new-user-field")
            this.$anonymousCheckbox = this.$(".anonymous-switcher")

            this.$anonymousCheckbox.switcher({
                on_state_content: '<i class="fa fa-check"></i>',
                off_state_content: '<i class="fa fa-times"></i>',
            })
            this.$(".switcher-toggler").html('<i class="fa fa-globe anonymous-switcher-knob">')

            this.createAccessView = this.createAccessView.bind(this)
            this.addAll = this.addAll.bind(this)

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
        },

        events: {
            "click .new-user-button": "finishUserInput",
            "keyup .new-user-field": "keyHandler"
        },
        keyHandler: function(e) {
            this.$newUserField.removeClass("has-error")
            if (e.keyCode === KEY_ENTER) {
                if (this.finishUserInput() === "done") {
                    sharePopup.closeAndSaveChanges()
                }
            } else if (e.keyCode === KEY_ESC) {
                if (this.$newUserField.val()) {
                    this.$newUserField.val("")
                } else {
                    sharePopup.closeAndDiscardChanges()
                }
            }
        },
        finishUserInput: function() {
            var newUser = this.$newUserField.val().trim()
            if (!newUser) { return "done" }

            if (!newUser.match(emailRegex)) {
                this.$newUserField.addClass("has-error")
                shake(this.$newUserField)
                Streamr.showError("Please enter an email-address!", null, 1000)
                return "error"
            }

            accessList.create({
                user: newUser,
                read: true,
            })
            this.$newUserField.val("")
            return "added"
        }
    })
    
    function shake($element) {
        if ($element.is(":animated")) { return }    // let's not mess with anyone else's animation...
        var ml = $element.css("margin-left")
        var mr = $element.css("margin-right")
        var d = 30;
        for (var i = 0; i < 10; i++) {
            d *= -0.7
            $element.animate({'margin-left': "+=" + d + 'px', 'margin-right': "-=" + d + 'px'}, 60);
        }
        $element.animate({'margin-left': ml, 'margin-right': mr}, 60);
    }

    // -------------------------------------------------------------
    //  Non-Backbone part that handles the sharingDialog lifecycle
    //      as well as communication with the backend
    // -------------------------------------------------------------

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
    var originalAnonPermission;

    /** Entry point */
    exports.sharePopup = function(url, resourceName) {
        if (dialogIsOpen()) { console.error("Cannot open sharePopup, already open!"); return false }
        if (!url) { console.error("Cannot open sharePopup without resource URL!"); return false }
        resourceUrl = url

        var originalPermissionList = []
        $.getJSON(resourceUrl + "/permissions").success(function(data) {
            originalPermissionList = _.filter(data, "id")
        }).always(function() {
            sharingDialog = bootbox.dialog({
                title: "Share <span class='resource-name-label'></span>",
                message: "Loading...",
                closeButton: true,
                className: "sharing-dialog",
                buttons: {
                    cancel: {
                        label: "Cancel",
                        className: "cancel-button",
                    },
                    save: {
                        label: "Save",
                        className: "btn-primary save-button",
                        callback: function() {
                            return listView.finishUserInput() !== "error" && saveChanges()
                        }
                    }
                }
            })
            $(".resource-name-label").text(resourceName || urlToResourceName(resourceUrl))

            // map list of Permissions to list of Access rows on the form (combine)
            originalPermissions = {}
            var initialAccessMap = {}
            originalAnonPermission = undefined;
            originalPermissionList.forEach(function(p) {
                if (!p || !p.operation) { return }      // continue
                if (p.anonymous) {
                    originalAnonPermission = p;
                } else if (p.user) {
                    if (!originalPermissions[p.user]) {
                        originalPermissions[p.user] = {}
                        initialAccessMap[p.user] = {user: p.user}
                    }
                    originalPermissions[p.user][p.operation] = p
                    initialAccessMap[p.user][p.operation] = true
                }
            })

            listView = new AccessListView({
                el: ".bootbox .modal-body",
                id: "access-list"
            })
            accessList.reset(_.values(initialAccessMap))

            // TODO: find out who is stealing the focus, then remove this kludge...
            setTimeout(function() { $(".new-user-field").focus() }, 500)
        })

        return false
    }

    // see http://stackoverflow.com/questions/19674701/can-i-check-if-bootstrap-modal-shown-hidden
    function dialogIsOpen() {
        return sharingDialog && sharingDialog.hasClass("in")
    }

    var pendingRequests = []
    function savingChanges() {
        pendingRequests = _.filter(pendingRequests, function(deferred) { return deferred.state() === "pending" })
        return pendingRequests.length > 0
    }

    function saveChanges() {
        if (savingChanges()) {
            Streamr.showInfo("Closing and saving changes, please wait...")
            return false;
        }

        // find changes
        var addedPermissions = []
        var removedPermissions = []
        var testedUsers = {}
        accessList.each(function(accessModel) {
            var user = accessModel.attributes.user
            var before = originalPermissions[user] || {}
            var after = accessModel.attributes
            if (before.read  && !after.read)  { removedPermissions.push(before.read) }
            if (before.write && !after.write) { removedPermissions.push(before.write) }
            if (before.share && !after.share) { removedPermissions.push(before.share) }
            if (!before.share && after.share) { addedPermissions.push({user: user, operation: "share"}) }
            if (!before.write && after.write) { addedPermissions.push({user: user, operation: "write"}) }
            if (!before.read  && after.read)  { addedPermissions.push({user: user, operation: "read"}) }
            testedUsers[user] = true;
        })
        // completely removed users don't show up in accessList, need to be tested separately
        _.each(originalPermissions, function (before, user) {
            if (user in testedUsers) { return }    // continue
            if (before.read)  { removedPermissions.push(before.read) }
            if (before.write) { removedPermissions.push(before.write) }
            if (before.share) { removedPermissions.push(before.share) }
        })

        var isAnon = listView.$anonymousCheckbox.prop("checked")

        // generate one API call for each change
        var started = 0
        var successful = 0
        var failed = 0
        var grantedTo = {}
        var revokedFrom = {}
        var errorMessages = []
        if (addedPermissions.length > 0 || removedPermissions.length > 0) {
            addedPermissions.forEach(function(permission) {
                started += 1
                pendingRequests.push($.ajax({
                    url: resourceUrl + "/permissions",
                    method: "POST",
                    data: JSON.stringify(permission),
                    contentType: "application/json",
                    error: onError,
                    success: function(response) {
                        grantedTo[response.user] = true
                        // update state so that the next diff goes correctly
                        if (!originalPermissions[response.user]) { originalPermissions[response.user] = {} }
                        originalPermissions[response.user][response.operation] = response
                        onSuccess()
                    }
                }))
            })
            removedPermissions.forEach(function(p) {
                started += 1
                pendingRequests.push($.ajax({
                    url: resourceUrl + "/permissions/" + p.id,
                    method: "DELETE",
                    contentType: "application/json",
                    error: onError,
                    success: function() {
                        revokedFrom[p.user] = true
                        // update state so that the next diff goes correctly
                        delete originalPermissions[p.user][p.operation]
                        onSuccess()
                    }
                }))
            })
        }
        if (isAnon && !originalAnonPermission) {
            started += 1
            pendingRequests.push($.ajax({
                url: resourceUrl + "/permissions",
                method: "POST",
                data: JSON.stringify({anonymous: true, operation: "read"}),
                contentType: "application/json",
                error: onError,
                success: function(response) {
                    grantedTo["the whole world"] = true
                    originalAnonPermission = response
                    onSuccess(response)
                }
            }))
        }
        if (!isAnon && !!originalAnonPermission) {
            started += 1
            pendingRequests.push($.ajax({
                url: resourceUrl + "/permissions/" + originalAnonPermission.id,
                method: "DELETE",
                contentType: "application/json",
                error: onError,
                success: function() {
                    revokedFrom["anonymous users"] = true
                    originalAnonPermission = undefined
                    onSuccess()
                }
            }))
        }
        function onSuccess() {
            successful += 1
            if (successful + failed === started) { onDone() }
        }
        function onError(e) {
            failed += 1
            errorMessages.push(e && e.responseJSON ? e.responseJSON.message : "Unknown error")
            if (successful + failed === started) { onDone() }
        }
        function onDone() {
            if (savingChanges()) { console.error("Requests not finished!") }
            if (successful > 0) {
                if (_.size(revokedFrom) > 0) {
                    Streamr.showSuccess("Successfully revoked access from " + _.keys(revokedFrom).join(", "))
                }
                if (_.size(grantedTo) > 0) {
                    Streamr.showSuccess("Successfully shared to " + _.keys(grantedTo).join(", "))
                }
            }
            if (successful === started) {
                // close after completely successful save
                sharingDialog.modal("hide")
            } else {
                Streamr.showError("Errors during saving:\n * " + errorMessages.join("\n * "))
            }
        }

        // don't close the dialog yet if there was something to save
        return started == 0
    }

    exports.sharePopup.closeAndSaveChanges = function() {
        if (!dialogIsOpen()) {
            console.error("Cannot close sharePopup, try opening it first!");
            return
        }
        if (saveChanges()) {
            sharingDialog.modal("hide")
        }
    }

    exports.sharePopup.closeAndDiscardChanges = function() {
        if (!dialogIsOpen()) {
            console.error("Cannot close sharePopup, try opening it first!");
            return
        }
        sharingDialog.modal("hide")
    }

    exports.sharePopup.isOpen = dialogIsOpen

})(window)
