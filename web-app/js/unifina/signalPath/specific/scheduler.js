(function(exports) {

//Change the variable signs from <%= var %> to {{ var }}
_.templateSettings = {
	evaluate : /\{\[([\s\S]+?)\]\}/g,
	escape : /\[\[([\s\S]+?)\]\]/g,
	interpolate : /\{\{([\s\S]+?)\}\}/g
};

var Range = Backbone.Model.extend({
	initialize: function(){
		this.set("start", {})
		this.set("end", {})
	},

	buildJSON: function(){
		var interval = this.get("interval")
		var intervalType = this.get("intervalType")
		var startDate = {
			minute: this.get("start").minute,
			hour: this.get("start").hour,
			day: this.get("start").day,
			month: this.get("start").month,
			weekday: this.get("start").weekday
		}
		var endDate = {
			minute: this.get("end").minute,
			hour: this.get("end").hour,
			day: this.get("end").day,
			month: this.get("end").month,
			weekday: this.get("end").weekday
		}
		var value = this.get("value")

		if(this.validate(value, startDate, endDate)){
			this.view.removeErrorHighlight()
			return {
				interval: interval,
				intervalType: intervalType,
				start: dateParser.formatDate(startDate),
				end: dateParser.formatDate(endDate),
				value: value
			}
		} else {
			this.view.errorHighlight()
		}
	},

	validate: function(value, startDate, endDate){
		if(!dateParser.validate(startDate, endDate)){
			// throw new Error("The end date must be after the start date!")
			return false
		} else if (!value) {
			// throw new Error("Ranges must have a value!")
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
		this.select(this.$el.find(".interval-type select option:selected").val())

		this.updateFields()
	},

	select: function(value){
		var _this = this
		var startContent = this.$el.eq(0).find("td.date")
		var endContent = this.$el.eq(2).find("td.date")
		this.$el.find(".date").empty()
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
		startContent.append(temp)
		endContent.append(temp)
		_this.$el.eq(0).find("select").change(function(){
			_this.updateFields()
		})
		_this.$el.eq(2).find("select").change(function(){
			_this.updateFields()
		})
	},

	updateFields: function(){
		var _this = this
		this.$el.eq(0).find(".date select").each(function(i, el){
			_this.model.get("start")[$(el).attr("name")] = parseInt($(el).find("option:selected").val())
		})
		this.$el.eq(2).find(".date select").each(function(i, el){
			_this.model.get("end")[$(el).attr("name")] = parseInt($(el).find("option:selected").val())
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
		}, 3000)
	},

	removeErrorHighlight: function(){
		this.$el.removeClass("error-highlight")
	}
})

var Scheduler = Backbone.View.extend({
	initialize: function(){
		var _this = this

		this.template = _.template($('#scheduler-template').html())
		this.collection = new RangeCollection()
		this.render()
	},
	render: function(){
		var _this = this
		this.$el.append(this.template)

		for(var i = 0; i < (this.collection.models.length || 1); i++){
			_this.addRange(_this.collection.models[i])
		}
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
			el: _this.$el.find("tbody")
		})
		rangeView.render(this.$el.find("tbody"))
	},

	buildJSON: function(){
		var _this = this

		var JSON = []
		_.each(_this.collection.models, function(model){
			var modelJSON = model.buildJSON()
			if(modelJSON != null)
				JSON.push(modelJSON)
		})
		return JSON
	}
})

function DateParser(){}

DateParser.prototype.formatDate = function(date){
	var month = date.month != undefined ? (date.month < 10 ? "0"+date.month : date.month) : "xx"
	var day = date.day != undefined ? (date.day < 10 ? "0"+date.day : date.day) : "xx"
	var hour = date.hour != undefined ? (date.our < 10 ? "0"+date.hour : date.hour) : "xx"
	var minute = date.minute != undefined ? (date.minute < 10 ? "0"+date.minute : date.minute) : "xx"
	var weekday = date.weekday != undefined ? date.weekday : ""
	
	if(weekday){
		return weekday +" "+ hour +":"+ minute
	} else {
		return day +"-"+ month +" "+ hour +":"+ minute
	}
}

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