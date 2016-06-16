(function() {

    var KeyValuePair = ListEditor.ValueInList.extend({
        defaults: {
            key: '',
            value: ''
        },
        isEmpty: function() {
            var v = this.get('key')
            return v === undefined || v === ""
        }
    });

    var KeyValuePairList = ListEditor.ValueList.extend({
        model: KeyValuePair,

        constructor: function(object) {
            object = object || {}
            var list = Object.keys(object).map(function(key) {
                return new KeyValuePair({
                    key: key,
                    value: object[key]
                })
            })
            // call superclass constructor
            ListEditor.ValueList.apply(this, [list]);
        },

        toJSON: function() {
            var list = this.filterEmpty()
            var map = {}
            list.forEach(function(item) {
                map[item.get('key')] = item.get('value')
            })
            return map
        }
    });

    var KeyValuePairView = ListEditor.ValueInListView.extend({
        template: '<td><input type="text" class="form-control input-sm key" name="key" value="{{key}}" placeholder="Key"></td><td><input type="text" class="form-control input-sm value" name="value" value="{{value}}" placeholder="Value"></td><td><button class="btn btn-default btn-xs delete"><i class="fa fa-trash-o"></i></button></td>',
        events: $.extend({}, ListEditor.ValueInListView.prototype.events, {
            'change input.key': 'update'
        }),
        update: function() {
            this.model.set("key", $(this.el).find("input.key").val())
            ListEditor.ValueInListView.prototype.update.apply(this)
        }
    });
    // Speeds up rendering
    Mustache.parse(KeyValuePairView.template)

    var KeyValuePairEditor = ListEditor.extend({
        template: "<table class='table table-striped table-condensed'><thead><tr><th>Key</th><th>Value</th><th><button class='btn btn-default btn-xs add'><i class='fa fa-plus'></i></button></th></tr></thead><tbody></tbody></table>",
        listItemType: KeyValuePair,
        listItemView: KeyValuePairView,
        collectionType: KeyValuePairList
    });
    // Speeds up rendering
    Mustache.parse(KeyValuePairEditor.template)

    KeyValuePairEditor.KeyValuePair = KeyValuePair
    KeyValuePairEditor.KeyValuePairList = KeyValuePairList
    KeyValuePairEditor.KeyValuePairView = KeyValuePairView

    if (typeof module !== 'undefined' && module.exports)
        module.exports = KeyValuePairEditor
    else window.KeyValuePairEditor = KeyValuePairEditor

})()