(function(exports) {

//Change the variable signs from <%= var %> to {{ var }}
_.templateSettings = {
	evaluate : /\{\[([\s\S]+?)\]\}/g,
	escape : /\[\[([\s\S]+?)\]\]/g,
	interpolate : /\{\{([\s\S]+?)\}\}/g
};

var Rule = Backbone.Model.extend({
	initialize: function(attributes, options){
		if($.isEmptyObject(attributes)){
			this.set("startDate", {})
			this.set("endDate", {
				minute: 1
			})
			this.set("intervalType", 0)
			this.set("value", 0)
		}
	},

	buildJSON: function(){
		var intervalType = this.get("intervalType")
		var value = this.get("value")
		var startDate = this.get("startDate")
		var endDate = this.get("endDate")

		return {
			intervalType: intervalType,
			startDate: startDate,
			endDate: endDate,
			value: value
		}
	},

	validate: function(){
		this.view.removeErrorHighlight()
		var startDate = {
			minute: this.get("startDate").minute,
			hour: this.get("startDate").hour,
			day: this.get("startDate").day,
			month: this.get("startDate").month,
			weekday: this.get("startDate").weekday
		}
		var endDate = {
			minute: this.get("endDate").minute,
			hour: this.get("endDate").hour,
			day: this.get("endDate").day,
			month: this.get("endDate").month,
			weekday: this.get("endDate").weekday
		}
		var value = this.get("value")
		if (!value && value != 0) {
			this.trigger("Error", {
				text: "Rules must have a value!"
			})
			this.view.errorHighlight()
			return false
		} else 
			return true
	},

	delete: function(){
		this.trigger("update")
		this.destroy({
			sync: false
		})
	}
})

var RuleCollection = Backbone.Collection.extend({
	model: Rule
})

var RuleView = Backbone.View.extend({
	initialize: function(){
		this.template = _.template($('#rule-view-template').html()),
		this.yearTemplate = _.template($('#rule-view-year-template').html()),
		this.monthTemplate = _.template($('#rule-view-month-template').html()),
		this.weekTemplate = _.template($("#rule-view-week-template").html()),
		this.dayTemplate = _.template($('#rule-view-day-template').html()),
		this.hourTemplate = _.template($('#rule-view-hour-template').html())

		this.model.view = this
	},

	render: function(table){
		var _this = this
		this.$el = $(this.template())

		table.append(this.$el)

		this.bindEvents()

		var select = this.$el.find("select[name='interval-type']")
		select.val(this.model.get("intervalType"))
		select.change()

		if(!$.isEmptyObject(this.model.get("startDate"))){
			var startRow = this.$el.find("div.date div.startDate")
			_.each(this.model.get("startDate"), function(val, key, i){
				var select = startRow.find("select[name='"+key+"']")
				select.val(val)
			})
		}

		if(!$.isEmptyObject(this.model.get("endDate"))){
			var endRow = this.$el.find("div.date div.endDate")
			_.each(this.model.get("endDate"), function(val, key){
				var select = endRow.find("select[name='"+key+"']")
				select.val(val)
			})
		}

		this.$el.find("input[name='value']").val(this.model.get("value"))

		this.updateFields()
	},

	bindEvents: function(){
		var _this = this
		this.$el.find("select[name='interval-type']").change(function(e){
			_this.select($(this).find("option:selected").val())
			_this.model.trigger("update")
		})

		this.$el.find("input[name='value']").on("keyup", function(e){
			_this.model.set("value", parseFloat($(this).val()))
		})

		this.$el.find(".delete-btn").click(function(){
			_this.delete()
		})
		this.$el.find(".move-up-btn").click(function(){
			_this.moveUp()
		})
		this.$el.find(".move-down-btn").click(function(){
			_this.moveDown()
		})
		this.model.on("active", function(i){
			if(i == 0)
				_this.$el.addClass("first")
			_this.$el.addClass("active-highlight")
		})
		this.model.on("unactive", function(){
			_this.$el.removeClass("first")
			_this.$el.removeClass("active-highlight")
		})
	},

	select: function(value){
		var _this = this
		var date = this.$el.find("div.date")
		date.empty()
		var temp
		if(value == 0){
			temp = this.hourTemplate
		} else if(value == 1){
			temp = this.dayTemplate
		} else if(value == 2){
			temp = this.weekTemplate
		} else if(value == 3){
			temp = this.monthTemplate
		} else if(value == 4){
			temp = this.yearTemplate
		}
		var start = $("<div/>", {
			class: "startDate",
			html: "<span>from</span>"
		})
		var end = $("<div/>", {
			class: "endDate",
			html: "<span>to</span>"
		})
		date.append(start)
		start.append(temp)
		date.append(end)
		end.append(temp)

		$(temp).find("select").change(function(){
			_this.updateFields()
		})
	},

	updateFields: function(){
		var _this = this
		this.model.set("startDate", {})
		this.model.set("endDate", {})
		this.$el.find(".date .startDate select").each(function(i, el){
			_this.model.get("startDate")[$(el).attr("name")] = parseInt($(el).find("option:selected").val())
		})
		this.$el.find(".date .endDate select").each(function(i, el){
			_this.model.get("endDate")[$(el).attr("name")] = parseInt($(el).find("option:selected").val())
		})
		this.model.set("interval", parseInt(_this.$el.find("select[name='interval'] option:selected").val()))
		this.model.set("intervalType", parseInt(_this.$el.find("select[name='interval-type'] option:selected").val()))
		this.model.set("value", parseFloat(_this.$el.find("input[name='value']").val()))
	},

	delete: function(){
		this.model.delete()
		this.remove()
	},

	errorHighlight: function(){
		var _this = this
		if(this.timeout){
			clearTimeout(this.timeout)
		}
		this.$el.addClass("error-highlight")
		this.timeout = setTimeout(function(){
			_this.$el.removeClass("error-highlight")
		}, 4000)
	},

	removeErrorHighlight: function(){
		this.$el.removeClass("error-highlight")
	},

	moveUp: function(){
		this.model.trigger("move-up", this.model)
		this.$el.hide()
		this.$el.insertBefore(this.$el.prev())
		this.$el.show("slow")
	},

	moveDown: function(){
		this.model.trigger("move-down", this.model)
		this.$el.hide()
		this.$el.insertAfter(this.$el.next())
		this.$el.show("slow")
	}
})

var Scheduler = Backbone.View.extend({
	initialize: function(options){
		var _this = this

		function templatesLoaded() {
			_this.template = _.template($('#scheduler-template').html())
			_this.footerTemplate = _.template($("#scheduler-footer-template").html())
			_this.collection = new RuleCollection()

			_this.collection.on("Error", function(msg){
				_this.trigger("Error", msg)
			})

			_this.defaultValue = 0

			if(options){
				if(options.footerEl)
					_this.footerEl = $(options.footerEl)
				if(options.schedule){
					_.each(options.schedule.rules, function(r){
						var rule = new Rule(r)
						_this.collection.add(rule)
					})
					_this.defaultValue = options.schedule.defaultValue
				}
			}

			_this.render()
			_this.bindEvents()
			_this.ready = true
			_this.trigger('ready')
		}

		// Load templates if they are not loaded
		if ($('#scheduler-template').length) {
			templatesLoaded()
		}
		else {
			Streamr.getResourceUrl('js/unifina/signalPath/specific','scheduler-template.html',false,function(url) {
				$.ajax({
					url: url,
					dataType: 'text',
					success: function(templates) {
						$("body").append(templates)
						templatesLoaded()
					}
				})
			})
		}
	},

	render: function(){
		var _this = this
		this.$el.html(this.template)
		$(this.footerEl).prepend(this.footerTemplate)

		for(var i = 0; i < (this.collection.models.length || 1); i++){
			_this.addRule(_this.collection.models[i])
		}

		this.defaultValueInput = this.footerEl.find("input[name='default-value']")
		this.defaultValueInput.val(this.defaultValue)
	},

	addRule: function(model){
		var _this = this
		var m
		if(model === undefined)
			m = new Rule({},{
				collection: _this.collection
			})
		else
			m = model
		this.collection.add(m)
		var ruleView = new RuleView({
			model: m,
			el: _this.$el.find("ol")
		})
		ruleView.render(this.$el.find("ol"))
	},

	buildJSON: function(){
		var _this = this

		if(this.validate()){
			var JSON = {}
			JSON.defaultValue = parseFloat(this.defaultValueInput.val())
			JSON.rules = []
			_.each(_this.collection.models, function(model){
				JSON.rules.push(model.buildJSON())
			})
			return JSON
		} else {
			throw 'Not valid!'
		}
	},

	validate: function(){
		var _this = this
		var validate = true
		if(!this.defaultValueInput.val()){
			this.errorHighlightDefault()
			this.trigger("Error", {
				text: "The scheduler needs a default value!"
			})
			validate = false
		}
		if(validate){
			_.each(_this.collection.models, function(model){
				if(!model.validate())
					validate = false
			})
		}
		return validate
	},

	errorHighlightDefault: function(){
		var _this = this
		if(this.timeout){
			clearTimeout(this.timeout)
		}
		this.defaultValueInput.addClass("error-highlight")
		this.timeout = setTimeout(function(){
			_this.defaultValueInput.removeClass("error-highlight")
		}, 4000)
	},

	highlightActives: function(activeRules){
		var _this = this
		var j = 0
		$.each(_this.collection.models, function(i, model){
			if(_.contains(activeRules, i)){
				model.trigger("active", j)
				j++;
			}
			else
				model.trigger("unactive")
		})
	},

	moveRuleUp: function(rule){
		var i = this.collection.models.indexOf(rule)
		this.collection.models.splice(i, 1)
		this.collection.models.splice(i-1, 0, rule)
	},

	moveRuleDown: function(rule){
		var i = this.collection.models.indexOf(rule)
		this.collection.models.splice(i, 1)
		this.collection.models.splice(i+1, 0, rule)
	},

	bindEvents: function(){
		var _this = this
		this.footerEl.find(".btn.add-rule-btn").click(function(){
			_this.addRule()
		})
		this.collection.on("move-up", function(rule){
			_this.moveRuleUp(rule)
		})
		this.collection.on("move-down", function(rule){
			_this.moveRuleDown(rule)
		})
		this.collection.on("update", function(){
			_this.trigger("update")
		})
	}
})

exports.Scheduler = Scheduler

})(typeof(exports) !== 'undefined' ? exports : window)