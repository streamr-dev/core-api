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

        toggle: function(op) {
            var self = this
            var oldState = this.get(op)
            if (typeof oldState !== "boolean") { return; }
            this.set(op, !oldState)
            if (oldState === false) { _(this.alsoAdd[op]).each(function(op2) { self.set(op2, true) }) }
            if (oldState === true) { _(this.alsoRemove[op]).each(function(op2) { self.set(op2, false) }) }
        },
        alsoAdd: {"write": ["read"], "share": ["read"]},
        alsoRemove: {"read": ["write", "share"]},
    })

    /** Access is shown as a row: username, operations (R, W, S), delete */
    var AccessView = Backbone.View.extend({
        events: {
            "click .toggle-permission": "togglePermission",
            "click .delete-button": function() { this.model.destroy() },
        },
        togglePermission: function(e) {
            var op = e.currentTarget.dataset.op || "read"
            this.model.toggle(op)
        },

        checkboxTemplate: _.template(
            '<label class="btn toggle-permission" data-op="<%= op %>">' +
                '<input type="checkbox"> <%= text %>' +
            '</label>'
        ),

        initialize: function() {
            var model = this.model

            this.$userLabel = $('<div id="user">').appendTo(this.$el)
            this.$permissions = $('<div class="btn-group" data-toggle="buttons">').appendTo(this.$el)
            this.$readLabel = $(this.checkboxTemplate({op: "read", text: "READ"})).appendTo(this.$permissions)
            //this.$readCheckbox = this.$readLabel.find("input")
            this.$writeLabel = $(this.checkboxTemplate({op: "write", text: "WRITE"})).appendTo(this.$permissions).find("input")
            this.$shareLabel = $(this.checkboxTemplate({op: "share", text: "SHARE"})).appendTo(this.$permissions).find("input")
            this.$deleteButton = $('<button class="btn btn-danger" id="delete-button">').text("Delete").on("click", function() { model.destroy() }).appendTo(this.$el)

            this.listenTo(model, 'change', this.render)
            this.listenTo(model, 'destroy', this.remove)
        },

        render: function() {
            this.$userLabel.text(this.model.get("user"))
            this.$readLabel.toggleClass("active", this.model.get("read"))
            this.$writeLabel.toggleClass("active", this.model.get("write"))
            this.$shareLabel.toggleClass("active", this.model.get("share"))
            //this.$readCheckbox.prop("checked", this.model.get("read"))
            //this.$writeCheckbox.prop("checked", this.model.get("write"))
            //this.$shareCheckbox.prop("checked", this.model.get("share"))
            return this
        },
    })

    /** Contents of the sharing popup dialog: list of Access rows */
    var AccessList = Backbone.Collection.extend({
        model: Access
    })
    var accessList = new AccessList()

    var AccessListView = Backbone.View.extend({
        events: {
            "click #new-user-button": "finishUserInput",
            "keypress #new-user": function(e) { if (e.which === KEY_ENTER) { this.finishUserInput() } }
        },
        finishUserInput: function() {
            var newUser = this.$newUserField.val()
            //TODO: check that user exists(?)
            if (newUser) {
                accessList.create({
                    user: newUser,
                    read: true,
                })
                this.$newUserField.val("")
            }
        },

        initialize: function(args) {
            this.$el.html("")
            this.$ownerLabel = $("<p>Owner: <span id='owner-label'>Lorem Ipsum</span></p>").appendTo(this.$el)
            // TODO: add existing Permissions
            this.$accessList = $("<div id='access-list'>").appendTo(this.$el)
            this.$newUserField = $("<input id='new-user' placeholder='Enter username' autofocus>").appendTo(this.$el)
            this.$newUserButton = $("<button class='btn btn-success' id='new-user-button'>").text("Add").appendTo(this.$el)

            this.listenTo(accessList, 'add', this.createAccessView)
            //this.listenTo(accessList, 'reset', this.addAll)  // initial
        },

        createAccessView: function(accessRow) {
            var view = new AccessView({model: accessRow})
            this.$accessList.append(view.render().$el)
        },
    })

    /** Entry point */
    function openSharingDialog(resourceUrl) {
        var sharingDialog = bootbox.dialog({
            title: "Share " + resourceUrl.split("/").slice(-2).join(" "),
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