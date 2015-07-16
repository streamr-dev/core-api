//Change the variable signs from <%= var %> to {{ var }}
_.templateSettings = {
	evaluate : /\{\[([\s\S]+?)\]\}/g,
	escape : /\[\[([\s\S]+?)\]\]/g,
	interpolate : /\{\{([\s\S]+?)\}\}/g
};

var Range = Backbone.Model.extend({

})

var RangeCollection = Backbone.Collection.extend({
	model: Range
})

var RangeView = Backbone.View.extend({
	initialize: function(){
		this.template = _.template($('#range-view-template').html()),
		this.monthsTemplate = _.template($('#range-view-months-template').html()),
		this.weekDaysTemplate = _.template($("#range-view-week-days-template").html()),
		this.daysTemplate = _.template($('#range-view-days-template').html()),
		this.clockTemplate = _.template($('#range-view-clock-template').html()),
		this.minutesFromTemplate = _.template($('#range-view-minutes-from-template').html())
	},

	render: function(){
		var _this = this
		this.$el.append(this.template)
		this.$el.find(".interval-type").change(function(e){
			_this.select($(this).find("select option:selected").val())
		})
	},

	select: function(value){
		this.$el.find("td:not(.base)").remove()
		if(value == 0){
			this.renderHour()
		} else if(value == 1){
			this.renderDay()
		} else if(value == 2){
			this.renderWeek()
		} else if(value == 3){
			this.renderMonth()
		} else if(value == 4){
			this.renderYear()
		}
	},

	renderYear: function(){
		this.$el.find("tr").append(this.daysTemplate)
		this.$el.find("tr").append(this.monthsTemplate)
		this.$el.find("tr").append(this.clockTemplate)
	},

	renderMonth: function(){
		this.$el.find("tr").append(this.daysTemplate)
		this.$el.find("tr").append(this.clockTemplate)
	},

	renderWeek: function(){
		this.$el.find("tr").append(this.weekDaysTemplate)
		this.$el.find("tr").append(this.clockTemplate)
	},

	renderDay: function(){
		this.$el.find("tr").append(this.clockTemplate)
	},

	renderHour: function(){
		this.$el.find("tr").append(this.minutesFromTemplate)
	}
})

var Scheduler = Backbone.View.extend({

	initialize: function(){
		this.template = _.template($('#scheduler-template').html())
		this.collection = new RangeCollection()
		this.rangeViews = []
		this.render()
	},
	render: function(){
		var _this = this
		this.$el.append(this.template)

		if(!this.collection.models.length){
			this.collection.add(new Range())
		}
		_.each(this.collection.models, function(model){
			var rangeView = new RangeView({
				model: model,
				el: _this.$el.find("tbody")
			})
			_this.rangeViews.push(rangeView)
			rangeView.render()
		})
	}
})