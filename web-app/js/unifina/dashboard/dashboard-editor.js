
var RunningSignalPath = Backbone.Model.extend({
	defaults: {
		name: "",
		uiChannels: ""
	}	
})

var RunningSignalPathList = Backbone.Collection.extend({
	model: RunningSignalPath
})

var RunningSignalPathView = Backbone.View.extend({
	tagName: "li",
	className: "runningsignalpath mm-dropdown mm-dropdown-root open",
	template: _.template($("#rsp-template").html()),

	events: {
		"click .rsp-title" : "openClose"
	},
	
	initialize: function (){
		this.uiChannelList = new UiChannelList(this.model.attributes.uiChannels)
	},

	render: function() {
		this.$el.html(this.template(this.model.toJSON()))
		this.$el.append(this.renderUIC())
		return this
	},

	renderUIC: function () {
		var channels = this.el, list = $("<ul/>", {
			class: "mmc-dropdown-delay animated fadeInLeft"
		})
		_.each(this.uiChannelList.models, function(item){
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

var UiChannel = Backbone.Model.extend({
	defaults: {
		name:"",
		id: "",
		checked: false,
	},
	toggle: function () {
		this.set({ checked: !this.get("checked") })
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
		"click .ui-title" : "toggleChecked"
	},

	initialize: function () {
		if(this.model.get("checked"))
			this.$el.addClass("checked")
	},

	render: function() {
		this.$el.html(this.template(this.model.toJSON()))
		return this
	},

	toggleChecked: function () {
		this.model.toggle();
		if (this.model.get("checked")) {
			this.$el.trigger("checked", [this.model])
			this.$el.addClass("checked")
		} else {
			this.$el.trigger("unchecked", [this.model])
			this.$el.removeClass("checked")
		}
	}
})

var SidebarView = Backbone.View.extend({
	el: $("#sidebar-view"),
	template: _.template($("#sidebar-template").html()),

	events: {
		"checked" : "updateDIList",
		"unchecked" : "updateDIList",
		"blur .title-input" : "updateTitle",
		"keypress .title-input" : "updateOnEnter",
		"click .save-button" : "save"
	},

	initialize: function (dashboard, RSPs) {
		this.dashboard = dashboard
		this.setData(RSPs, dashboard.collection)
		this.listenTo(dashboard.collection, "removed", function(DI){
			this.removeDashboardItem(DI)
		})
		this.render()
	},

	render: function () {
		this.$el.html(this.template())
		this.$el.find(".title").append($("<input/>", {
			class: "title-input form-control",
			type: "text",
			name: this.dashboard.get("name"),
			value: this.dashboard.get("name"),
			placeholder: "Dashboard name"
		}))
		var list = $("<ul/>", {
			class: "navigation",
			id: "rsp-list"
		})
		this.$el.find(".content").append(list)
		_.each(this.collection.models, function(item) {
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
		this.collection = new RunningSignalPathList(RSPs)
	},

	updateDIList: function(event, uiChannelModel) {
		if(event.type == "checked") {
			_.each(this.collection.models, function(runningsignalpath){
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

	removeDashboardItem: function (DI) {
		_.each(this.collection.models, function(RSP) {
			_.each(RSP.get("uiChannels"), function(UIC) {
				if(UIC.id == DI.get("uiChannel").id) {
					UIC.checked = false
				}
			})
		})
		// $("#toggle-"+DI.get("uiChannel").id).removeAttr("checked")
		DI.destroy()
		this.render()
	},

	updateTitle: function (e) {
		this.title = $("#title").val()
	},

	updateOnEnter: function(e) {
      if (e.keyCode == 13) this.updateTitle();
    }
})

var DashboardItem = Backbone.Model.extend({
	defaults: {
		title: "",
		uiChannel: {id: "", name: "", module: {id: ""}},
		type: ""
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
	model: DashboardItem
})

var DashboardItemView = Backbone.View.extend({
	tagName: "div",
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
		this.listenTo(this.model, "destroy", this.remove)
	},

	render: function() {
		this.$el.html(this.template(this.model.toJSON()))
		if(this.model.get("type") == "chart") {
			this.$el.addClass("col-xs-12 col-sm-12 col-md-8 col-lg-6 col-centered")
			this.$el.find(".widget-content").append(this.chartTemplate(this.model.toJSON()))
		}
		else if(this.model.get("type") == "label") {
			this.$el.addClass("col-xs-12 col-sm-6 col-md-4 col-lg-3 col-centered")
			this.$el.find(".widget-content").append(this.labelTemplate(this.model.toJSON()))
		}
		else if(this.model.get("type") == "heatmap") {
			this.$el.addClass("col-xs-12 col-sm-12 col-md-8 col-lg-6 col-centered")
			this.$el.find(".widget-content").append(this.heatmapTemplate(this.model.toJSON()))
		}
		else {
			console.log("Module not recognized!")
		}
		this.$el.find(".title").append(this.titlebarTemplate(this.model.toJSON()))
		return this
	},

	delete: function () {
		this.model.collection.trigger("removed", this.model)
	},

	toggleEdit: function () {
		if(this.$el.hasClass("editing"))
			this.model.set("title", this.$el.find(".name-input").val())
		this.$el.toggleClass("editing")
		this.$el.find(".title").empty()
		this.$el.find(".title").append(this.titlebarTemplate(this.model.toJSON()))
	},

	updateOnEnter: function(e) {
      if (e.keyCode == 13) this.toggleEdit();
    }
})

var Dashboard = Backbone.Model.extend({
	defaults: {
		name: "",
		items: []
	},

	initialize: function (dashboard) {
		this.collection = new DashboardItemList(dashboard.items)
	}
})

var DashboardView = Backbone.View.extend({
	el: "#dashboard-view",
	initialize: function(dashboard) {
		this.collection = dashboard.collection

		this.render()

		// $(this.el).droppable({
  //           drop: function(ev, ui){
  //               // get reference to dropped view's model
  //               var model = $(ui.draggable).data("backbone-view").model;
  //           },
  //       });

		this.collection.on("add", function(model){
			this.$el.append(new DashboardItemView({
				model: model
			}).render().el)
		},this)
		this.collection.on("remove", function(model){
			model.destroy()
		},this)
	},

	render: function() {
		this.$el.find("div").remove()

		_.each(this.collection.models, function(item) {
			this.renderDI(item)
		},this)
	},

	renderDI: function(item) {
		var DIView = new DashboardItemView({
			model: item
		})
		this.$el.append(DIView.render().el)
        // DIView.$el.draggable();
        DIView.$el.data("backbone-view", this);
	},
})

