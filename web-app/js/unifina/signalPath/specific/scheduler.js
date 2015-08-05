(function(exports) {

//Change the variable signs from <%= var %> to {{ var }}
_.templateSettings = {
	evaluate : /\{\[([\s\S]+?)\]\}/g,
	escape : /\[\[([\s\S]+?)\]\]/g,
	interpolate : /\{\{([\s\S]+?)\}\}/g
};

var Range = Backbone.Model.extend({
	initialize: function(attributes, options){
		if($.isEmptyObject(attributes)){
			this.set("startDate", {})
			this.set("endDate", {})
			this.set("intervalType", 0)
			this.set("value", 0)
		}
	},

	buildJSON: function(){
		var interval = this.get("interval")
		var intervalType = this.get("intervalType")
		var value = this.get("value")
		var startDate = this.get("startDate")
		var endDate = this.get("endDate")

		return {
			interval: interval,
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
		if(!dateParser.validate(startDate, endDate)){
			this.trigger("Error", {
				text: "The end date must be after the start date!"
			})
			this.view.errorHighlight()
			return false
		} else if (!value) {
			this.trigger("Error", {
				text: "Ranges must have a value!"
			})
			this.view.errorHighlight()
			return false
		} else 
			return true
	},

	delete: function(){
		this.destroy({
			sync: false
		})
	}
})

var RangeCollection = Backbone.Collection.extend({
	model: Range
})

var RangeView = Backbone.View.extend({
	initialize: function(){
		this.template = _.template($('#range-view-template').html()),
		this.yearTemplate = _.template($('#range-view-year-template').html()),
		this.monthTemplate = _.template($('#range-view-month-template').html()),
		this.weekTemplate = _.template($("#range-view-week-template").html()),
		this.dayTemplate = _.template($('#range-view-day-template').html()),
		this.hourTemplate = _.template($('#range-view-hour-template').html())

		this.model.view = this
	},

	render: function(table){
		var _this = this
		this.$el = $(this.template())

		this.$el.find("select[name='interval-type']").change(function(e){
			_this.select($(this).find("option:selected").val())
		})

		this.$el.find("input[name='value']").on("keyup", function(e){
			_this.model.set("value", $(this).val())
		})

		this.$el.find(".delete-btn").click(function(){
			_this.delete()
		})

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
		this.model.set("value", _this.$el.find("input[name='value']").val())
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
	}
})

var Scheduler = Backbone.View.extend({
	initialize: function(options){
		var _this = this


		this.template = _.template($('#scheduler-template').html())
		this.collection = new RangeCollection()

		this.collection.on("Error", function(msg){
			_this.trigger("Error", msg)
		})

		var options
		options.config = {
			defaultValue: 3,
			ranges: [{
				value: 100,
				intervalType: 1,
				startDate: {
					hour: 10,
					minute: 25
				},
				endDate: {
					hour: 11,
					minute: 35
				}
			}, {
				value: 200,
				intervalType: 2,
				startDate: {
					weekday: 2,
					hour: 10,
					minute: 25
				},
				endDate: {
					weekday: 3,
					hour: 11,
					minute: 35
				}
			}]
		}

		this.defaultValue = 2

		if(options && options.config){
			_.each(options.config.ranges, function(r){
				var range = new Range(r)
				_this.collection.add(range)
			})
			this.defaultValue = options.config.defaultValue
		}

		this.render()

		this.bindEvents()
	},
	render: function(){
		var _this = this
		this.$el.html(this.template)

		for(var i = 0; i < (this.collection.models.length || 1); i++){
			_this.addRange(_this.collection.models[i])
		}

		this.defaultValueInput = this.$el.find("input[name='default']")
		this.defaultValueInput.val(this.defaultValue)
	},

	addRange: function(model){
		var _this = this
		var m
		if(model === undefined)
			m = new Range({},{
				collection: _this.collection
			})
		else
			m = model
		this.collection.add(m)
		var rangeView = new RangeView({
			model: m,
			el: _this.$el.find("ol")
		})
		rangeView.render(this.$el.find("ol"))
	},

	buildJSON: function(){
		var _this = this

		if(this.validate()){
			var JSON = {}
			JSON.ranges = []
			JSON.defaultValue = this.defaultValueInput.val()
			_.each(_this.collection.models, function(model){
				JSON.ranges.push(model.buildJSON())
			})
			console.log(JSON)
			return JSON
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
		_.each(_this.collection.models, function(model){
			if(!model.validate())
				validate = false
		})
		return validate
	},

	bindEvents: function(){
		var _this = this
		this.$el.find(".btn.add-range-btn").click(function(){
			_this.addRange()
		})
		this.$el.find(".btn.validate-btn").click(function(){
			_this.buildJSON()
		})
	}
})

function DateParser(){}

DateParser.prototype.validate = function(startDate, endDate){
	if(startDate.month === undefined || startDate.month >= endDate.month){
		if((startDate.weekday && startDate.weekday == endDate.weekday) || (startDate.day && startDate.day == endDate.day) || (startDate.weekday === undefined && startDate.day === undefined)){
			if(startDate.hour === undefined || startDate.hour == endDate.hour){
				if(startDate.minute > endDate.minute)
					return false
			} else if(startDate.hour > endDate.hour)
				return false
		} else if((startDate.day && startDate.day > endDate.day) || (startDate.weekday && startDate.weekday > endDate.weekday))
			return false
	} else if(startDate.month > endDate.month)
		return false
	
	return true
}

var dateParser = new DateParser()

exports.dateParser = dateParser
exports.Scheduler = Scheduler

})(typeof(exports) !== 'undefined' ? exports : window)