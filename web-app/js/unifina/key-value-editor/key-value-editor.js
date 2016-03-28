(function(exports) {

    var KeyValuePair = Backbone.Model.extend({
        defaults: {
            key: '',
            value: ''
        }
    });

    var KeyValuePairList = Backbone.Collection.extend({
        model: KeyValuePair,

        fromJSON: function(map) {
            var _this = this
            Object.keys(map).forEach(function(key) {
                _this.add(new KeyValuePair({
                    key: key,
                    value: map[key]
                }))
            })
        },

        toJSON: function() {
            var map = {}
            this.models.forEach(function(model) {
                if (model.get('key') !== '') {
                    map[model.get('key')] = model.get('value')
                }
            })
            return map
        }
    });

    // TODO: how to tab from key to value?
    var KeyValuePairView = Backbone.View.extend({
        tagName: 'tr',
        template: '<td><input type="text" class="form-control input-sm key" name="key" value="{{key}}" placeholder="Key"></td><td><input type="text" class="form-control input-sm value" name="value" value="{{value}}" placeholder="Value"></td><td><button class="btn btn-default btn-xs delete"><i class="fa fa-trash-o"></i></button></td>',
        events: {
            'click .delete': 'remove',
            'change input.key': 'update',
            'change input.value': 'update',
        },
        initialize: function() {
            this.listenTo(this.model, 'change', this.render)
            this.listenTo(this.model, 'remove', this.unrender)
        },
        render: function() {
            this.$el.html(Mustache.render(this.template, this.model.attributes))
            return this
        },
        update: function() {
            this.model.set("key", $(this.el).find("input.key").val())
            this.model.set("value", $(this.el).find("input.value").val())
        },
        unrender: function(){
            $(this.el).remove();
        },
        remove: function(){
            this.model.collection.remove(this.model);
        }
    });
    Mustache.parse(KeyValuePairView.template)

    var KeyValuePairEditor = Backbone.View.extend({
        template: "<table class='table table-striped table-condensed'><thead><tr><th>Key</th><th>Value</th><th><button class='btn btn-default btn-xs add'><i class='fa fa-plus'></i></button></th></tr></thead><tbody></tbody></table>",
        events: {
            'click .add': 'add'
        },
        initialize: function() {
            if (!this.collection) {
                this.collection = new KeyValuePairList()
            }

            this.listenTo(this.collection, 'add', this.append)
            this.render();
        },
        render: function() {
            var _this = this
            this.$el.html(Mustache.render(this.template))
            this.collection.models.forEach(function(model) {
                _this.append(model);
            });

            return this
        },
        getAddButton: function() {
          return this.$el.find(".add")
        },
        add: function() {
            var item = new KeyValuePair();
            this.collection.add(item);
        },
        append: function(model) {
            var view = new KeyValuePairView({
                model: model
            });
            this.$el.find('table tbody').append(view.render().el);
        },
        clear: function() {
            this.collection.remove(this.collection.toArray())
        },
        disable: function() {
            this.$el.hide()
        },
        enable: function() {
            this.$el.show()
        }
    });
    Mustache.parse(KeyValuePairEditor.template)

    exports.KeyValuePairList = KeyValuePairList
    exports.KeyValuePairEditor = KeyValuePairEditor

})(typeof(exports) !== 'undefined' ? exports : window)