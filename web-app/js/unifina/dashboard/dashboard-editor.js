
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
		'change .toggle': 'toggleChecked',
	},

	render: function() {
		this.$el.html(this.template(this.model.toJSON()))
		return this
	},

	toggleChecked: function () {
		this.model.toggle();
		if (this.model.get("checked"))
			this.$el.trigger("checked", [this.model])
		else this.$el.trigger("unchecked", [this.model])
	}
})

var SidebarView = Backbone.View.extend({
	el: $("#sidebar-view"),

	events: {
		'checked': 'updateDIList',
		'unchecked': 'updateDIList',
	},

	initialize: function (dashboardName, RSPs, DIs) {
		this.dashboardName = dashboardName
		this.setData(RSPs, DIs)
		this.render()
	},

	render: function () {
		this.$el.find("li").remove()
		this.$el.find("input").remove()
		this.$el.append($("<input/>", {
			class: "title form-control form-group-margin",
			type: "text",
			name: this.dashboardName,
			placeholder: this.dashboardName
		}))

		_.each(this.collection.models, function(item) {
			this.renderRSP(item)
		}, this)
	},

	renderRSP: function(item) {
		var runningSignalPathView = new RunningSignalPathView({
			model: item
		})
		var list = $("<ul/>", {
			id: "rsp-list"
		})
		this.$el.append(list)
		list.append(runningSignalPathView.render().el)
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
						this.DIList.push({title: uiChannelModel.get("name"), uiChannel: uiChannelModel})
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
	}
})

var DashboardItem = Backbone.Model.extend({
	defaults: {
		title: "",
		uiChannel: {id: ""}
	}
})

var DashboardItemList = Backbone.Collection.extend({
	model: DashboardItem
})

var DashboardItemView = Backbone.View.extend({
	tagName: "div",
	className: "dashboarditem",	
	template: _.template($("#streamr-label-template").html()),

	events: {
		// 'click .destroy': 'destroy',
	},

	initialize: function(){
		that = this
		this.listenTo(this.model, "destroy", this.remove)
	},

	render: function() {
		this.$el.html(this.template(this.model.toJSON()))
		console.log("New dashboardItem created!")
		return this
	},

	removeView: function() {
      this.$el.remove(); /* off to unbind the events */
      this.stopListening();
      return this;
	}
})

var DashboardView = Backbone.View.extend({
	el: "#dashboard-view",
	initialize: function(DIList) {
		this.collection = DIList

		this.render()

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
	},
})

