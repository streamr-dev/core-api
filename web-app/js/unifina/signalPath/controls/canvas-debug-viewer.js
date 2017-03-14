var ModuleDebugMessage = Backbone.Model.extend({});

var ModuleDebugMessageList = Backbone.Collection.extend({
    model: ModuleDebugMessage,

    initialize: function(options) {
        options = options || {};
        this.maxSize = options.maxSize || 30;

        var _this = this
        this.on("add", function() {
            if (_this.length > _this.maxSize) {
                _this.pop();
                _this.trigger("pop");
            }
        });
    }
});

var ModuleDebugView = Backbone.View.extend({

    tagName: "tr",

    template: _.template(
        '<td><a href="#module_{{ moduleHash}}">{{ moduleName }}</a></td>' +
        '{[ if (this.adhoc) { ]}' +
            '<td>{{ canvasTime }}</td>' +
        '{[ } ]}' +
        '<td>{{ serverTime }}</td>' +
        '<td>{{ msg }}</td>'
    ),

    render: function() {
        this.$el.html(this.template(this.model.toJSON()));
        return this;
    }
});

var CanvasDebugViewer = Backbone.View.extend({

    tagName: "table",

    initialize: function(options) {
        this.collection = options.collection;
        this.isAdhoc = options.isAdhoc;
        this.render();
        this.collection.bind("add", this._addRender.bind(this));
        this.collection.bind("pop", this._popRender.bind(this));
        this.collection.bind("reset", this.render.bind(this));
    },

    render: function() {
        this.$thead = $("<thead>");
        this.$tbody = $("<tbody>");
        this.$el.empty().append(this.$thead, this.$tbody);
        this.collection.each(this._addRender.bind(this));
    },

    _addRender: function(model) {
        if (this.collection.length === 1) {
            this._addHeader();
        }

        var view = new ModuleDebugView({
            model: model,
            adhoc: this.isAdhoc()
        });
        this.$tbody.prepend(view.render().el);
    },

    _popRender: function() {
        this.$tbody.find("tr:last").remove();
    },


    _addHeader: function() {
        var headerTitles;
        if (this.isAdhoc()) {
            headerTitles = ["Module", "Time on Canvas", "Time on Server", "Message"];
        } else {
            headerTitles = ["Module", "Time on Server", "Message"];
        }

        var $headerTr = $("<tr>");
        _.each(headerTitles, function (key) {
            $("<th>", {text: key}).appendTo($headerTr);
        });

        this.$thead.empty().append($headerTr);
    }
});