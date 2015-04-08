
var UiChannel = Backbone.Model.extend({
	defaults: {
		name:"",
		id: "",
		checked: false,
	},
	toggle: function () {
		this.set("checked", !this.get("checked"))
	}
})
  
var UiChannelList = Backbone.Collection.extend({
	model: UiChannel
})

var UiChannelView = Backbone.View.extend({
	tagName: "li",
	className: "uichannel",	
	template: _.template($("#uichannel-template").html()),

	events: {
		"click .uichannel-title" : "toggleChecked"
	},

	initialize: function () {
		if(this.model.get("checked"))
			this.$el.addClass("checked")
		this.listenTo(this.model, "change:checked", this.render)
	},

	render: function() {
		this.$el.html(this.template(this.model.toJSON()))
		if (this.model.get("checked")) {
			this.$el.addClass("checked")
		} else {
			this.$el.removeClass("checked")
		}
		return this
	},

	toggleChecked: function () {
		this.model.toggle();
		if (this.model.get("checked")) {
			this.$el.trigger("checked", [this.model])
		} else {
			this.$el.trigger("unchecked", [this.model])
		}
	}
})

var RunningSignalPath = Backbone.Model.extend({
	defaults: {
		name: "",
		uiChannels: ""
	},
	initialize: function (){
		this.howManyChecked = 0
		this.uiChannelCollection = new UiChannelList(this.get("uiChannels"))
		_.each(this.uiChannelCollection.models, function(model){
			if(model.get("checked"))
				this.howManyChecked++
		},this)
	}
})

var RunningSignalPathList = Backbone.Collection.extend({
	model: RunningSignalPath
})

var RunningSignalPathView = Backbone.View.extend({
	tagName: "li",
	className: "runningsignalpath mm-dropdown mm-dropdown-root",
	template: _.template($("#rsp-template").html()),

	events: {
		"click .rsp-title" : "openClose"
	},
	
	initialize: function (){
		this.model.uiChannelCollection.on("change:checked", function(model){
			if(model.get("checked") == true)
				this.model.howManyChecked++
			else {
				this.model.howManyChecked--
			}
			this.render()
		},this)
	},

	render: function() {
		if(!this.$el.children().length) {
			this.$el.append(this.template(this.model.toJSON()))
			this.$el.append(this.renderUIC())
		}
		if(this.model.howManyChecked)
			this.$el.find(".howmanychecked").html(this.model.howManyChecked)
		else this.$el.find(".howmanychecked").empty()
		return this
	},

	renderUIC: function () {
		var channels = this.el, list = $("<ul/>", {
			class: "mmc-dropdown-delay animated fadeInLeft"
		})
		_.each(this.model.uiChannelCollection.models, function(item){
			var uiChannelView = new UiChannelView({
				model: item
			})
			list.append(uiChannelView.render().el)
		}, this)
		return list
	},

	openClose: function() {
		this.$el.toggleClass("open")
	}
})

var DashboardItem = Backbone.AssociatedModel.extend({
	defaults: {
		title: "",
		uiChannel: {id: "", name: "", module: {id: ""}},
		type: "",
		order: ""
	},

	initialize: function() {
		id = this.get("uiChannel").module.id
		if(id == 67) {
			this.set("type", "chart")
		} else if(id == 145) {
			this.set("type", "label")
		} else if(id == 196) {
			this.set("type", "heatmap")
		}
	}
})

var DashboardItemList = Backbone.Collection.extend({
	model: DashboardItem,
	comparator: "order"
})

var DashboardItemView = Backbone.View.extend({
	tagName: "li",
	className: "dashboarditem",	
	template: _.template($("#streamr-widget-template").html()),
	labelTemplate: _.template($("#streamr-label-template").html()),
	chartTemplate: _.template($("#streamr-chart-template").html()),
	heatmapTemplate: _.template($("#streamr-heatmap-template").html()),
	titlebarTemplate: _.template($("#titlebar-template").html()),

	events: {
		"click .delete" : "delete",
		"click .edit" : "toggleEdit",
		"click .close-edit" : "toggleEdit",
		"blur .name-input" : "toggleEdit",
		"keypress .name-input" : "updateOnEnter"
	},

	initialize: function(){
		this.model.on("remove", this.remove)
	},

	render: function() {
		this.$el.html(this.template(this.model.toJSON()))
		if(this.model.get("type") == "chart") {
			// this.$el.find("div:first").addClass("col-xs-12 col-sm-12 col-md-8 col-lg-6 col-centered")
			this.$el.addClass("col-xs-12 col-sm-12 col-md-8 col-lg-6 col-centered")
			this.$el.find(".widget-content").append(this.chartTemplate(this.model.toJSON()))
		}
		else if(this.model.get("type") == "label") {
			//this.$el.find("div:first").addClass("col-xs-12 col-sm-6 col-md-4 col-lg-3 col-centered")
			this.$el.addClass("col-xs-12 col-sm-6 col-md-4 col-lg-3 col-centered")
			this.$el.find(".widget-content").append(this.labelTemplate(this.model.toJSON()))
		}
		else if(this.model.get("type") == "heatmap") {
			//this.$el.find("div:first").addClass("col-xs-12 col-sm-12 col-md-8 col-lg-6 col-centered")
			this.$el.addClass("col-xs-12 col-sm-12 col-md-8 col-lg-6 col-centered")
			this.$el.find(".widget-content").append(this.heatmapTemplate(this.model.toJSON()))
		}
		else {
			console.log("Module not recognized!")
		}
		this.$el.find(".title").append(this.titlebarTemplate(this.model.toJSON()))
		this.initializeTrigger()
		return this
	},

	initializeTrigger: function() {
		this.listenTo(this.$el, "drop")
	},

	delete: function () {
		this.model.collection.remove(this.model)
	},

	toggleEdit: function () {
		// if(this.$el.find("div:first").hasClass("editing"))
		if(this.$el.hasClass("editing"))
			this.model.set("title", this.$el.find(".name-input").val())
		// this.$el.find("div:first").toggleClass("editing")
		this.$el.toggleClass("editing")
		this.$el.find(".title").empty()
		this.$el.find(".title").append(this.titlebarTemplate(this.model.toJSON()))
	},

	updateOnEnter: function(e) {
      if (e.keyCode == 13) this.toggleEdit();
    }
})

var SidebarView = Backbone.View.extend({
	el: $("#sidebar-view"),
	template: _.template($("#sidebar-template").html()),

	events: {
		"checked" : "updateDIList",
		"unchecked" : "updateDIList",
		"blur #dashboard-name" : "updateTitle",
		"keypress #dashboard-name" : "updateOnEnter",
		"click .save-button" : "save"
	},

	initialize: function (dashboard, RSPs) {
		this.dashboard = dashboard
		this.setData(RSPs, dashboard.get("items"))
		this.listenTo(dashboard.get("items"), "remove", function(DI){
			this.uncheck(DI.get("uiChannel").id)
		})
		this.render()
	},

	render: function () {
		this.$el.html(this.template())
		var title = $("<input/>", {
			class: "title-input form-control",
			type: "text",
			id: "dashboard-name",
			name: "dashboard-name",
			value: this.dashboard.get("name"),
			placeholder: "Dashboard name"
		})
		var list = $("<ul/>", {
			class: "navigation",
			id: "rsp-list"
		})		
		this.$el.find(".title").append(title)
		this.$el.find(".content").append(list)
		_.each(this.rspCollection.models, function(item) {
			list.append(this.renderRSP(item).el)
		}, this)
	},

	renderRSP: function(item) {
		var runningSignalPathView = new RunningSignalPathView({
			model: item
		})
		return runningSignalPathView.render()
	},

	setData: function(RSPs, DIList) {
		this.DIList = DIList

		var checked = {}
		_.each(this.DIList.models, function(item) {
			checked[item.get("uiChannel").id] = true
		})
		_.each(RSPs, function(rsp) {
			_.each(rsp.uiChannels, function(uiChannel) {
				if (checked[uiChannel.id])
					uiChannel.checked = true
			})
		})
		this.rspCollection = new RunningSignalPathList(RSPs)
	},

	updateDIList: function(event, uiChannelModel) {
		if(event.type == "checked") {
			_.each(this.rspCollection.models, function(runningsignalpath){
				_.each(runningsignalpath.get("uiChannels"), function(uiChannel) {
					if(uiChannel.id == uiChannelModel.get("id")) {
						this.DIList.push({title: uiChannelModel.get("name"), uiChannel: uiChannel})
					}
				},this)
			},this)
		}
		if(event.type == "unchecked") {
			var list = _.filter(this.DIList.models, function(item) {
				return item.get("uiChannel").id == uiChannelModel.get("id")
			}, this)
			this.DIList.remove(list)
		}
	},

	uncheck: function (id) {
		_.each(this.rspCollection.models, function(RSP) {
			_.each(RSP.uiChannelCollection.models, function(uiChannelModel){
				if(uiChannelModel.get("id") == id)
					uiChannelModel.set("checked", false)
			})
		})
	},

	updateTitle: function (e) {
		this.dashboard.set("name", $("#dashboard-name").val())
	},

	updateOnEnter: function(e) {
      if (e.keyCode == 13) this.updateTitle();
    },

    save: function() {
    	this.dashboard.save()
    }
})

var Dashboard = Backbone.AssociatedModel.extend({
	relations: [
        {
            type: Backbone.Many,
            key: 'items',
            collectionType: DashboardItemList,
            relatedModel: DashboardItem
        }
    ],

	defaults: {
		name: "",
		items: []
	}
})

var DashboardView = Backbone.View.extend({
	el: "#dashboard-view",

	initialize: function(dashboard) {
		this.dashboard = dashboard
		this.dashboardItemViews = []
		
		this.$el.sortable({
			stop: function(event, ui) {
				var i = ui.item.index()
	            ui.item.find(".contains").trigger('dropped', i)
        	}
		})

		_.each(this.dashboard.get("items").models, this.addDashboardItem, this)

		this.dashboard.get("items").on("add", this.addDashboardItem, this)
		this.dashboard.get("items").on("remove", this.removeDashboardItem, this)
	},

	addDashboardItem: function(model) {
		var DIView = new DashboardItemView({
			model: model
		})
		this.$el.append(DIView.render().el)
		this.dashboardItemViews.push(DIView)
	},

	removeDashboardItem: function(model) {
		var viewsToRemove = _.filter(this.dashboardItemViews, function(DIView) {
			return DIView.model === model
		})
		viewsToRemove.forEach(function(view) { view.remove() })
		this.dashboardItemViews = _.without(this.dashboardItemViews, viewsToRemove)
	}
})