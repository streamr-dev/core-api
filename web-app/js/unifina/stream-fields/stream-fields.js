(function(exports) {

	var TYPES = ['number','string','boolean','map','list']

	// **Item class**: The atomic part of our Model. A model is basically a Javascript object, i.e. key-value pairs, with some helper functions to handle event triggering, persistence, etc.
	var Item = Backbone.Model.extend({
		defaults: {
			name: 'name',
			type: TYPES[0]
		}
	});

	// **List class**: A collection of `Item`s. Basically an array of Model objects with some helper functions.
	var List = Backbone.Collection.extend({
		model: Item
	});

	var ItemView = Backbone.View.extend({
		tagName: 'tr', // name of (orphan) root tag in this.el
	    events: {
	      'click span.delete': 'remove',
	      'change select': 'update',
	      'blur input': 'update'
	    },
		initialize: function(){
		  _.bindAll(this, 'render', 'unrender', 'remove', 'update'); // every function that uses 'this' as the current object should be in here

		  this.model.bind('remove', this.unrender);
		  this.model.bind('change', this.render);
		},
		render: function() {
			var $types = $("<select name='field_type' class='form-control input-sm'></select>")
			$types.append(TYPES.map(function(type) {
				return $("<option value='"+type+"'>"+type+"</option>")
			}))
			$(this.el).html('<td class="name"><input type="text" class="form-control input-sm" name="field_name" value="'+this.model.get('name')+'"></td><td class="types">'+this.model.get('type')+'</td><td><span class="btn btn-sm delete fa fa-trash-o delete-field-button"></span></td>');
			$types.val(this.model.get('type'))
			$(this.el).find("td.types").html($types)
			return this; // for chainable calls, like .render().el
		},
		update: function() {
			this.model.set("name", $(this.el).find("input").val())
			this.model.set("type", $(this.el).find("select").val())
		},
		unrender: function(){
	    	$(this.el).remove();
	    },
	    remove: function(){
	    	this.model.collection.remove(this.model);
	    }
	});

	var ListView = Backbone.View.extend({
		events: {
			'click button.add': 'addItem',
			'click button.save': 'save'
		},
		// `initialize()` now instantiates a Collection, and binds its `add` event to own method `appendItem`. (Recall that Backbone doesn't offer a separate Controller for bindings...).
		initialize: function(){
			_.bindAll(this, 'render', 'addItem', 'appendItem', 'save'); // remember: every function that uses 'this' as the current object should be in here

			if (this.id===undefined)
				throw "Must provide stream.id as 'id'!"

			this.collection = new List();
			this.collection.url = Streamr.createLink("stream","fields",this.id)
			this.collection.bind('add', this.appendItem); // collection event binder

			this.render();
		},
		render: function(){
			// Save reference to `this` so it can be accessed from within the scope of the callback below
			var self = this;

			var $table = $("<table class='table table-striped table-condensed'><thead><tr><th>Name</th><th>Type</th><th></th></tr></thead><tbody></tbody></table>")
			$(this.el).html($table);
			this.collection.models.forEach(function(item){ // in case collection is not empty
				self.appendItem(item);
			}, this);

			$(this.el).append("<button class='add btn btn-sm pull-right'><i class='fa fa-plus'></i> Add Field</button>");
			$(this.el).append("<button class='save btn btn-lg btn-primary'><i class='fa fa-save'></i> Save</button>");
		},
		save: function() {
			var _this = this
			this.collection.sync("create", this.collection, {
				success: function() {
					console.log("Saved!")
					_this.trigger('saved')
				},
				error: function(xhr, err, err2) {
					throw "Error while saving: "+err+", "+err2
				}
			})
		},
		// `addItem()` now deals solely with models/collections. View updates are delegated to the `add` event listener `appendItem()` below.
		addItem: function(){
			var item = new Item();
			this.collection.add(item); // add item to collection; view is updated via event 'add'
		},
	    appendItem: function(item){
	      var itemView = new ItemView({
	        model: item
	      });
	      $('table tbody', this.el).append(itemView.render().el);
	    },
	    clear: function() {
	    	this.collection.remove(this.collection.toArray())
	    }
	});

	exports.ListView = ListView
})(window);