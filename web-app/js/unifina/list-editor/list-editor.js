(function() {

    function parseValue(value) {
        // try to parse
        try {
            return JSON.parse(value)
        } catch (err) {}

        return value
    }

    // from http://stackoverflow.com/questions/3362471/how-can-i-call-a-javascript-constructor-using-call-or-apply
    function newInstanceOf(constructor) {
        var instance = Object.create(constructor.prototype);
        var result = constructor.apply(instance, Array.prototype.slice.call(arguments, 1));
        return (result !== null && typeof result === 'object') ? result : instance;
    }

    var ValueInList = Backbone.Model.extend({
        defaults: {
            value: ''
        },
        constructor: function(valueOrAttrs) {
            Backbone.Model.apply(this, [{value: parseValue(value)}]);
        },
        toJSON: function() {
            return this.get('value')
        },
        isEmpty: function() {
            var v = this.get('value')
            return v === undefined || v === ""
        }
    });

    var ValueList = Backbone.Collection.extend({
        model: ValueInList,

        // Wrap values
        constructor: function(values) {
            ListEditor.ValueList.apply(this, [values.map(function(value) {
                return {
                    value: parseValue(value)
                }
            })]);
        },
        
        toJSON: function() {
            // Filter empty values
            var filtered = this.models.filter(function(model) {
                return !model.isEmpty()
            })

            var converted = filtered.map(function(model) {
                return model.toJSON()
            })
        }
    });

    var ValueInListView = Backbone.View.extend({
        tagName: 'tr',
        template: '<td><input type="text" class="form-control input-sm value" name="value" value="{{value}}" placeholder="Value"></td><td><button class="btn btn-default btn-xs delete"><i class="fa fa-trash-o"></i></button></td>',
        events: {
            'click .delete': 'remove',
            'change input.value': 'update'
        },
        initialize: function() {
            this.listenTo(this.model, 'remove', this.unrender)
        },
        render: function() {
            this.$el.html(Mustache.render(this.template, this.model.attributes))
            return this
        },
        update: function() {
            this.model.set("value", parseValue($(this.el).find("input.value").val()))
        },
        unrender: function() {
            $(this.el).remove();
        },
        remove: function() {
            this.model.collection.remove(this.model);
        }
    });
    // Speeds up rendering
    Mustache.parse(ValueInListView.template)

    var ListEditor = Backbone.View.extend({
        // Wrap table in a form to get native tabbing
        template: "<table class='table table-striped table-condensed'><thead><tr><th>Value</th><th><button class='btn btn-default btn-xs add'><i class='fa fa-plus'></i></button></th></tr></thead><tbody></tbody></table>",
        events: {
            'click .add': 'add'
        },
        listItemType: ValueInList,
        listItemView: ValueInListView,
        collectionType: ValueList,

        initialize: function() {
            if (!this.collection) {
                this.collection = this.createCollection([])
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
            var item = newInstanceOf(this.listItemType)
            this.collection.add(item);
        },
        append: function(model) {
            var view = newInstanceOf(this.listItemView, {
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
        },
        createCollection: function(list) {
            return newInstanceOf(this.collectionType, list)
        }
    });
    // Speeds up rendering
    Mustache.parse(ListEditor.template)

    ListEditor.ValueInList = ValueInList
    ListEditor.ValueInListView = ValueInListView
    ListEditor.ValueList = ValueList

    if (typeof module !== 'undefined' && module.exports)
        module.exports = ListEditor
    else window.ListEditor = ListEditor

})()