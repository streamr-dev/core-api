(function(exports) {
	
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
		this.template = _.template(Templates['rule-view-template']),
		this.yearTemplate = _.template(Templates['rule-view-year-template']),
		this.monthTemplate = _.template(Templates['rule-view-month-template']),
		this.weekTemplate = _.template(Templates["rule-view-week-template"]),
		this.dayTemplate = _.template(Templates['rule-view-day-template']),
		this.hourTemplate = _.template(Templates['rule-view-hour-template'])

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
		this.model.on("inactive", function(){
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

		_this.template = _.template(Templates['scheduler-template'])
		_this.footerTemplate = _.template(Templates["scheduler-footer-template"])
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
				model.trigger("inactive")
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

	var Templates = {}

	Templates["scheduler-template"] = ''+
		'<ol class="table scheduler-table">'+

		'</ol>';


	Templates["scheduler-footer-template"] = ''+
		'<div class="setup col-xs-12">'+
			'<div class="add-rule">'+
				'<button class="btn add-rule-btn btn-primary"><i class="fa fa-plus" />&nbsp;Add</button>'+
			'</div>'+
			'<div class="default-value">'+
				'<span>Default value:</span><input type="number" step="any" name="default-value" class="form-control input-default input-sm" value="0"/>'+
			'</div>'+
		'</div>';

	Templates["rule-view-template"] = ''+
		'<li class="rule">'+
			'<div class="td">Every</div>'+
			'<div class="td interval-type">'+
				'<select name="interval-type" class="form-control input-sm">'+
					'<option value="0">hour</option>'+
					'<option value="1">day</option>'+
					'<option value="2">week</option>'+
					'<option value="3">month</option>'+
					'<option value="4">year</option>'+
				'</select>'+
			'</div>'+
			'<div class="td date"></div>'+
			'<div class="td value">'+
				'Value: <input name="value" type="number" step="any" class="form-control input-sm" value="0.0"/>'+
			'</div>'+
			'<div class="td move">'+
				'<div class="move-up-btn" title="Move up">'+
					'<i class=" fa fa-caret-up"></i>'+
				'</div>'+
				'<div class="move-down-btn" title="Move down">'+
					'<i class=" fa fa-caret-down"></i>'+
				'</div>'+
			'</div>'+
			'<div class="td delete">'+
				'<i class="delete-btn fa fa-trash-o" title="Remove"></i>'+
			'</div>'+
		'</li>';

	Templates["rule-view-year-template"] = ''+
		'the'+
		'<select name="day" class="form-control input-sm">'+
			'{[ _.each(_.range(31), function(i){ ]}'+
				'<option value="{{i+1}}">'+
					'{[ if(i+1 == 1 || (i+1) % 10 == 1){ ]} '+
						'{{ i+1 + "st" }}'+
					'{[ } else if(i+1 == 2 || (i+1) % 10 == 2) { ]}'+
						'{{ i+1 + "nd" }}'+
					'{[ } else if(i+1 == 3 || (i+1) % 10 == 3) { ]}'+
						'{{ i+1 + "rd" }}'+
					'{[ } else { ]}'+
						'{{ i+1 + "th" }}'+
					'{[ } ]}'+
				'</option>'+
			'{[ }); ]}'+
		'</select>'+
		'of'+
		'<select name="month" class="form-control input-sm">'+
			'<option value="1">January</option>'+
			'<option value="2">February</option>'+
			'<option value="3">March</option>'+
			'<option value="4">April</option>'+
			'<option value="5">May</option>'+
			'<option value="6">June</option>'+
			'<option value="7">July</option>'+
			'<option value="8">August</option>'+
			'<option value="9">September</option>'+
			'<option value="10">October</option>'+
			'<option value="11">November</option>'+
			'<option value="12">December</option>'+
		'</select>'+
		'at'+
		'<select name="hour" class="form-control input-sm">'+
			'{[ _.each(_.range(24), function(i){ ]}'+
				'<option value="{{i}}">'+
					'{[ if(i<10){ ]} '+
						'0{{i}}'+
					'{[ } else { ]}'+
						'{{i}}'+
					'{[ } ]}'+
				'</option>'+
			'{[ }); ]}'+
		'</select>'+
		':'+
		'<select name="minute" class="form-control input-sm">'+
			'{[ _.each(_.range(60), function(i){ ]}'+
				'<option value="{{i}}">'+
					'{[ if(i<10){ ]} '+
						'0{{i}}'+
					'{[ } else { ]}'+
						'{{i}}'+
					'{[ } ]}'+
				'</option>'+
			'{[ }); ]}'+
		'</select>';

	Templates["rule-view-month-template"] = ''+
		'the'+
		'<select name="day" class="form-control input-sm">'+
			'{[ _.each(_.range(31), function(i){ ]}'+
				'<option value="{{i+1}}">'+
					'{[ if(i+1 == 1 || (i+1) % 10 == 1){ ]} '+
						'{{ i+1 + "st" }}'+
					'{[ } else if(i+1 == 2 || (i+1) % 10 == 2) { ]}'+
						'{{ i+1 + "nd" }}'+
					'{[ } else if(i+1 == 3 || (i+1) % 10 == 3) { ]}'+
						'{{ i+1 + "rd" }}'+
					'{[ } else { ]}'+
						'{{ i+1 + "th" }}'+
					'{[ } ]}'+
				'</option>'+
			'{[ }); ]}'+
		'</select>'+
		'at'+
		'<select name="hour" class="form-control input-sm">'+
			'{[ _.each(_.range(24), function(i){ ]}'+
				'<option value="{{i}}">'+
					'{[ if(i<10){ ]} '+
						'0{{i}}'+
					'{[ } else { ]}'+
						'{{i}}'+
					'{[ } ]}'+
				'</option>'+
			'{[ }); ]}'+
		'</select>'+
		':'+
		'<select name="minute" class="form-control input-sm">'+
			'{[ _.each(_.range(60), function(i){ ]}'+
				'<option value="{{i}}">'+
					'{[ if(i<10){ ]} '+
						'0{{i}}'+
					'{[ } else { ]}'+
						'{{i}}'+
					'{[ } ]}'+
				'</option>'+
			'{[ }); ]}'+
		'</select>';

	Templates["rule-view-week-template"] = ''+
		'<select name="weekday" class="form-control input-sm">'+
			'<option value="2">Monday</option>'+
			'<option value="3">Tuesday</option>'+
			'<option value="4">Wednesday</option>'+
			'<option value="5">Thursday</option>'+
			'<option value="6">Friday</option>'+
			'<option value="7">Saturday</option>'+
			'<option value="1">Sunday</option>'+
		'</select>'+
		'at'+
		'<select name="hour" class="form-control input-sm">'+
			'{[ _.each(_.range(24), function(i){ ]}'+
				'<option value="{{i}}">'+
					'{[ if(i<10){ ]} '+
						'0{{i}}'+
					'{[ } else { ]}'+
						'{{i}}'+
					'{[ } ]}'+
				'</option>'+
			'{[ }); ]}'+
		'</select>'+
		':'+
		'<select name="minute" class="form-control input-sm">'+
			'{[ _.each(_.range(60), function(i){ ]}'+
				'<option value="{{i}}">'+
				'{[ if(i<10){ ]} '+
					'0{{i}}'+
				'{[ } else { ]}'+
					'{{i}}'+
				'{[ } ]}'+
			'</option>'+
			'{[ }); ]}'+
		'</select>';

	Templates["rule-view-day-template"] = ''+
		'<select name="hour" class="form-control input-sm">'+
			'{[ _.each(_.range(24), function(i){ ]}'+
				'<option value="{{i}}">'+
					'{[ if(i<10){ ]} '+
						'0{{i}}'+
					'{[ } else { ]}'+
						'{{i}}'+
					'{[ } ]}'+
				'</option>'+
			'{[ }); ]}'+
		'</select>'+
		':'+
		'<select name="minute" class="form-control input-sm">'+
			'{[ _.each(_.range(60), function(i){ ]}'+
				'<option value="{{i}}">'+
					'{[ if(i<10){ ]} '+
						'0{{i}}'+
					'{[ } else { ]}'+
						'{{i}}'+
					'{[ } ]}'+
				'</option>'+
			'{[ }); ]}'+
		'</select>';

	Templates["rule-view-hour-template"] = ''+
		'<select name="minute" class="form-control input-sm">'+
			'{[ _.each(_.range(60), function(i){ ]}'+
				'<option value="{{i}}">'+
					'{[ if(i<10){ ]} '+
						'0{{i}}'+
					'{[ } else { ]}'+
						'{{i}}'+
					'{[ } ]}'+
				'</option>'+
			'{[ }); ]}'+
		'</select>'+
		'minutes from the beginning of the hour';


exports.Scheduler = Scheduler

})(typeof(exports) !== 'undefined' ? exports : window)