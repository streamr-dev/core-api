/**
 * Dialog that shows users and their permissions to given resource
 *
 * Can be spawned with something like
 *  <button onclick="sharePopup('${createLink(uri: "/api/v1/stream/" + stream.uuid)}')"> Share </button>
 *
 * @create_date 2016-01-21
 */

(function(exports) {

    /** Access by a user to a resource is defined by 0..3 Permissions */
    var Access = Backbone.Model.extend({
        defaults: {
            user: "",
            read: false,
            write: false,
            share: false
        },

        toggle: function(op) {
            if (typeof this.get(op) !== "boolean") { return; }
            this.save(_.object([op, !this.get(op)]))
        }
    })

    /** Access is shown as a row: username, operations (R, W, S), delete */
    var AccessView = Backbone.View.extend({
        events: {
            "click .toggle-permission": "togglePermission",
            "blur .new-access": "closeUsernameInput",
            "keypress .new-access": function(e) { if (e.keyCode == 13) { this.closeUsernameInput() } },
            "click .delete-access": function() { this.model.destroy() },
        },

        initialize: function() {
            var $userLabel = $("<div id='user'>").appendTo(this.$el)
            var $readCheckbox = $("<div id='read' class='toggle-permission'>").appendTo(this.$el)
            var $writeCheckbox = $("<div id='write' class='toggle-permission'>").appendTo(this.$el)
            var $shareCheckbox = $("<div id='share' class='toggle-permission'>").appendTo(this.$el)

            this.listenTo(this.model, 'change', this.render)
            this.listenTo(this.model, 'destroy', this.remove)
        },

        render: function() {
            $userLabel.text(this.model.user)
            $readCheckbox.prop("checked", this.model.read)
            $writeCheckbox.prop("checked", this.model.write)
            $shareCheckbox.prop("checked", this.model.share)
        },

        togglePermission: function(e) {
            var op = e.currentTarget.id || "read"
            this.model.toggle(op)
        },

        closeUsernameInput: function() {
            var newName = this.input.val()
            if (!value) {
                this.model.destroy()
            } else {
                this.model.save({user: newName})
            }
        },
    })

    /** Contents of the sharing popup dialog: list of Access rows */
    var AccessList = Backbone.Collection.extend({
        model: Access
    })
    var accessList = new AccessList()

    var AccessListView = Backbone.View.extend({
        initialize: function(args) {
            var $ownerLabel = $("<p>Owner:<span id='owner-label'></span></p>").appendTo(this.$el)
            // TODO: add existing Permissions
            var $accessList = $("<div id='access-list'>")
            var $newUserField = $("<input id='new-access-user' placeholder='Enter username' autofocus>").appendTo(this.$el)
            this.listenTo(accessList, 'add', this.add)
            //this.listenTo(accessList, 'reset', addAll)  // initial
        },

        add: function(accessRow) {
            var view = new AccessView({model: accessRow})
            this.$accessList.append(view.render.$el)
        }
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
            el: "#sharing-view-div",
            id: "sharing-view",
        })
    }

    exports.sharePopup = openSharingDialog

})(window)