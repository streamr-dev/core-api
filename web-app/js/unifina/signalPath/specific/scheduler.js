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
			this.set("endDate", {})
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
		if(!dateValidator.validate(startDate, endDate)){
			this.trigger("Error", {
				text: "The end date must be after the start date!"
			})
			this.view.errorHighlight()
			return false
		} else if (!value && value != 0) {
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

		this.bindEvents()

		table.append(this.$el)

		var select = this.$el.find("select[name='interval-type']")
		select.val(this.model.get("intervalType"))
		select.change()

		if(!$.isEmptyObject(this.model.get("startDate"))){
			var startRow = this.$el.find("div.date div.startDate")
			_.each(this.model.get("startDate"), function(val, key){
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

		_this.$el.find("select").change(function(){
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
		this.$el.insertBefore(this.$el.prev())
	},

	moveDown: function(){
		this.model.trigger("move-down", this.model)
		this.$el.insertAfter(this.$el.next())
	}
})

var Scheduler = Backbone.View.extend({
	initialize: function(options){
		var _this = this


		this.template = _.template($('#scheduler-template').html())
		this.collection = new RuleCollection()

		this.collection.on("Error", function(msg){
			_this.trigger("Error", msg)
		})

		this.defaultValue = 0

		if(options && options.schedule){
			_.each(options.schedule.rules, function(r){
				var rule = new Rule(r)
				_this.collection.add(rule)
			})
			this.defaultValue = options.schedule.defaultValue
		}

		this.render()

		this.bindEvents()
	},

	render: function(){
		var _this = this
		this.$el.html(this.template)

		for(var i = 0; i < (this.collection.models.length || 1); i++){
			_this.addRule(_this.collection.models[i])
		}

		this.defaultValueInput = this.$el.find("input[name='default-value']")
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
		this.$el.find(".btn.add-rule-btn").click(function(){
			_this.addRule()
		})
		this.$el.find(".btn.validate-btn").click(function(){
			_this.buildJSON()
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

function DateValidator(){}

DateValidator.prototype.validate = function(startDate, endDate){
	if(startDate.month === undefined || startDate.month == endDate.month){
		if((startDate.weekday != undefined && startDate.weekday == endDate.weekday) 
			|| (startDate.day != undefined && startDate.day == endDate.day) 
			|| (startDate.weekday === undefined && startDate.day === undefined)){
			if(startDate.hour === undefined || startDate.hour == endDate.hour){
				if(startDate.minute >= endDate.minute)
					return false
			} else if(startDate.hour > endDate.hour)
				return false
		} else if((startDate.day && startDate.day > endDate.day) || (startDate.weekday && startDate.weekday > endDate.weekday))
			return false
	} else if(startDate.month > endDate.month)
		return false
	return true
}

var dateValidator = new DateValidator()

exports.dateValidator = dateValidator
exports.Scheduler = Scheduler

})(typeof(exports) !== 'undefined' ? exports : window)