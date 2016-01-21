/**
 * Dialog that
 *
 * Is opened by pressing a "share" button such as following:
 * <button class="share" data-resource="/api/v1/stream/116207">
 *     data-resource contains URL of the resource
 *
 */

(function(exports) {

    // access by a user to a resource is defined by 0..3 permissions
    var Access = Backbone.Model.extend({
        defaults: {
            user: "",
            read: false,
            write: false,
            share: false
        },

        toggle: function(op) {
            var newPermission = {}
            newPermission[op] = !this.get(op)
            this.save(newPermission)
        }
    })

    var AccessList = Backbone.Collection.extend({
        model: Access
    })

    var AccessView = Backbone.View.extend({
        events: {
            "keypress .new-access": "closeOnEnter",
            "blur .new-access": "closeUsernameInput",
            "click .delete-access": "deleteAccess",
            "click .toggle-permission": "togglePermission",
        },

        closeOnEnter: function(e) {
            if (e.keyCode == 13) { this.closeUsernameInput() }
        },

        closeUsernameInput: function() {
            var newName = this.input.val()
            if (!value) {
                this.deletePermission()
            } else {
                this.model.save({user: newName})
            }
        },

        toggleReadPermission: function(e) {
            //var op = $(e.currentTarget).data("operation") || "read"
            var op = e.currentTarget.dataSet.operation || "read"
            this.model.toggle(op)
        },

        deleteAccess: function() {
            this.model.destroy()
        }
    })

    exports.SharingDialog = Backbone.View.extend({
        events: {
            "click #done": "close"
        },

        initialize: function(args) {
            bootbox.dialog({
                title: "Sharing options",
                message: "Share " + args.resourceUrl,
                size: 'large'
            })
        },
    })

})(window)