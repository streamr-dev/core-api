(function(exports) {

//Change the variable signs from <%= var %> to {{ var }}
_.templateSettings = {
	evaluate : /\{\[([\s\S]+?)\]\}/g,
	escape : /\[\[([\s\S]+?)\]\]/g,
	interpolate : /\{\{([\s\S]+?)\}\}/g
};

var UiChannel = Backbone.Model.extend({
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
	initialize: function (){
		this.uiChannelCollection = new UiChannelList(this.get("uiChannels"))
	},
	getCheckedCount: function () {
		var howManyChecked = 0
		_.each(this.uiChannelCollection.models, function(model){
			if(model.get("checked"))
				howManyChecked++
		},this)
		return howManyChecked
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
			this.render()
		},this)
	},

	render: function() {
		if(!this.$el.children().length) {
			this.$el.append(this.template(this.model.toJSON()))
			this.$el.append(this.renderUIC())
		}
		if(this.model.getCheckedCount())
			this.$el.find(".howmanychecked").html(this.model.getCheckedCount())
		else 
			this.$el.find(".howmanychecked").empty()
		if(this.model.get("state") == 'stopped'){
			this.$el.addClass("stopped")
		}
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
	makeSmall: function() {
		if(this.get("size") != "small")
			this.set("size", "small")
	},

	makeMedium: function() {
		if(this.get("size") != "medium")
			this.set("size", "medium")
	},

	makeLarge: function() {
		if(this.get("size") != "large")
			this.set("size", "large")
	}
})

var DashboardItemList = Backbone.Collection.extend({
	model: DashboardItem
})

var DashboardItemView = Backbone.View.extend({
	tagName: "li",
	className: "dashboarditem",	
	template: _.template($("#streamr-widget-template").html()),
	titlebarTemplate: _.template($("#titlebar-template").html()),

	events: {
		"click .delete-btn" : "deleteDashBoardItem",
		"click .titlebar-clickable" : "toggleEdit",
		"click .edit-btn" : "toggleEdit",
		"click .close-edit" : "toggleEdit",
		"keypress .name-input" : "toggleEdit",
		"blur .name-input" : "toggleEdit",
		"click .make-small-btn" : "changeSize",
		"click .make-medium-btn" : "changeSize",
		"click .make-large-btn" : "changeSize"
	},

	render: function() {
		var _this = this
		
		this.smallClass = "small-size col-xs-12 col-sm-6 col-md-4 col-lg-3 col-centered"
		this.mediumClass = "medium-size col-xs-12 col-sm-12 col-md-8 col-lg-6 col-centered"
		this.largeClass = "large-size col-xs-12 col-centered"

		var type = this.model.get("uiChannel").module.id
		this.$el.html(this.template(this.model.toJSON()))
		if(type == 145) {
			if(!this.model.get("size"))
				this.model.set("size", "small")
		}
		else  {
			if(!this.model.get("size"))
				this.model.set("size", "medium")
		}
		var templateName = "#" + this.model.get("uiChannel").module.webcomponent + "-template"
		var template = _.template($(templateName).html())
		this.$el.find(".widget-content").append(template(this.model.toJSON()))

		var titlebar = this.titlebarTemplate(this.model.toJSON())
		this.$el.find(".title").append(titlebar)
		this.initSize()
		this.$el.find(".make-" +this.model.get("size")+ "-btn").parent().addClass("checked")
		
		// Pass error events from webcomponents onto the model
		this.$el.find(".streamr-widget").on('error', function(e) {
			_this.model.trigger('error', e.originalEvent.detail.error, _this.model.get('title'))
		})
		
		this.streamrWidget = this.$el.find(".streamr-widget")

		this.bindEvents()

		return this
	},

	deleteDashBoardItem: function(){
		this.model.collection.remove(this.model)
	},

	bindEvents: function(){
		var _this = this
		this.listenTo(this.model, "resize", function() {
			_this.streamrWidget[0].dispatchEvent(new Event("resize"))
		})
		this.$el.on("drop", function(e, index) {
			_this.model.collection.trigger("orderchange")
		})
	},

	remove: function () {
		this.streamrWidget[0].dispatchEvent(new Event("remove"))
		Backbone.View.prototype.remove.apply(this)
	},

	toggleEdit: function (e) {
		_this = this
		_this.editOff = function () {
			if(this.$el.hasClass("editing")){
					this.model.set("title", this.$el.find(".name-input").val())
					this.$el.find(".titlebar").html(this.model.get("title"))
					this.$el.find(".titlebar-clickable").html(this.model.get("title"))
					this.$el.removeClass("editing")
				}
		}
		_this.editOn = function() {
			if(!(this.$el.hasClass("editing"))){
					this.$el.addClass("editing")
					setTimeout(function(){
					    _this.$el.find(".name-input").focus();
					}, 0);
				}
		}
		if(e.type == "click") {
			if($(e.currentTarget).hasClass("edit-btn") || $(e.currentTarget).hasClass("titlebar-clickable")) {
				_this.editOn()
			} else if($(e.currentTarget).hasClass("close-edit")){
				_this.editOff()
			}
		} else if(e.type == "focusout" || e.type == "blur"){ //='blur'
			_this.editOff()
		} else if(e.keyCode == 13) {
			_this.editOff()
		}
			
	},

    initSize: function() {
    	this.$el.removeClass(this.smallClass+ " " +this.mediumClass+ " " +this.largeClass)
    	var size = this.model.get("size")
    	if(size == "small" || size == "medium" || size == "large") {
    		if(size == "small") {
    			this.$el.addClass(this.smallClass)
	    	} else if(size == "medium") {
	    		this.$el.addClass(this.mediumClass)
	    	} else if(size == "large") {
	    		this.$el.addClass(this.largeClass)
    		}
    	} else 
    		throw new Error("Module size not found")
    },

    changeSize: function(e) {
    	this.$el.find(".make-" +this.model.get("size")+ "-btn").parent().removeClass("checked")
    	if($(e.target).hasClass("make-small-btn"))
    		this.model.makeSmall()
    	else if($(e.target).hasClass("make-medium-btn"))
    		this.model.makeMedium()
    	else if($(e.target).hasClass("make-large-btn"))
    		this.model.makeLarge()
    	this.initSize()
    	this.$el.find(".make-" +this.model.get("size")+ "-btn").parent().addClass("checked")
    	this.$el.find(".widget-content").children()[0].dispatchEvent(new Event("resize"))
    }
})

var SidebarView = Backbone.View.extend({
	events: {
		"checked" : "updateDIList",
		"unchecked" : "updateDIList",
		"click .save-button" : "save"
	},

	initialize: function (options) {
		this.el = options.el
		this.dashboard = options.dashboard
		this.allRSPs = options.RSPs
		this.menuToggle = options.menuToggle

		this.RSPs = []
		_.each(this.allRSPs, function(rsp){
			rsp.uiChannels = _.filter(rsp.uiChannels, function(uic){
				return uic.module
			})
			if(rsp.uiChannels.length)
				this.RSPs.push(rsp)
		},this)
		this.setData(this.RSPs, this.dashboard.get("items"))
		this.listenTo(this.dashboard.get("items"), "remove", function(DI){
			this.uncheck(DI.get("uiChannel").id)
		})
		var _this = this
		this.dashboard.get("items").on("change", function () {
			_this.dashboard.saved = false
		})
		this.menuToggle.click(function () {
			if($("body").hasClass("editing"))
				_this.setEditMode(false)
			else
				_this.setEditMode(true)
		})
		if(this.dashboard.get("items").models.length)
			this.setEditMode(options.edit)
		else this.setEditMode(true)
		this.render()
		this.$el.find(".dashboard-name").change(function () {
			_this.dashboard.saved = false
		})

		this.dashboard.on('invalid', function(error) {
			console.log(error)
			Streamr.showError(_this.dashboard.validationError, 'Invalid value')
		})
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

	render: function () {
		this.title = $("<div/>", {
			class: "menu-content",
			html: "<label>Dashboard name</label>"
		})
		this.titleInput = $("<input/>", {
			class: "dashboard-name title-input form-control",
			type: "text",
			name: "dashboard-name",
			value: this.dashboard.get("name"),
			placeholder: "Dashboard name"
		})
		this.rspTitle = $("<li/>", {
			class: "rsp-title",
			html: "<label>Running Signalpaths</label>"
		})
		this.list = $("<ul/>", {
			class: "navigation",
			id: "rsp-list"
		})
		var buttonTemplate = _.template($("#button-template").html())
		this.buttons = $(buttonTemplate(this.dashboard.toJSON()))
		this.$el.append(this.title)
		this.title.append(this.titleInput)
		this.$el.append(this.list)
		this.list.append(this.rspTitle)
		_.each(this.rspCollection.models, function(item) {
			this.list.append(this.renderRSP(item).el)
		}, this)
		this.$el.append(this.buttons)
		new Toolbar(this.buttons.find("form"))
	},

	renderRSP: function(item) {
		var runningSignalPathView = new RunningSignalPathView({
			model: item
		})
		return runningSignalPathView.render()
	},

	setEditMode: function (active) {		
		if (active || active===undefined && !$("body").hasClass("mmc")) {
			$("body").addClass("mme")
			$("body").removeClass("mmc")
			$("body").addClass("editing")
		} else {
			$("body").addClass("mmc")
			$("body").removeClass("mme")
			$("body").removeClass("editing")
		}

    	$("body").trigger("classChange")

    	_.each(this.dashboard.get("items").models, function(item) {
    		item.trigger("resize")
    	},this)
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
		var a = this.titleInput.val()
		this.dashboard.set({name: a})
	},

    save: function() {
    	var _this = this
    	this.updateTitle()

    	this.dashboard.save({}, {
    		success: function() {
	    		_this.dashboard.saved = true
	    		document.title = _this.dashboard.get("name")
				Streamr.showSuccess('Dashboard ' +_this.dashboard.get("name")+ ' saved successfully', "Saved!")
	    	},
	    	error: function(model, response) {
				Streamr.showError(response.responseText, "Error while saving")
	    	}
	    })
    },

    triggerClassChange: function () {
    	$("body").trigger("classChange")
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

	validate: function() {
		if (!this.get("name"))
			return "Dashboard name can't be empty"
	},

	initialize: function (){
		this.saved = true
	}
})

var DashboardView = Backbone.View.extend({

	initialize: function(options) {
		var _this = this
		this.dashboardItemViews = []
		// Avoid needing jquery ui in tests
		if (this.$el.sortable) {
			this.$el.sortable({
				cancel: ".non-draggable, .titlebar-edit, .panel-heading-controls",
				items: ".dashboarditem",
				stop: function(event, ui) {
					ui.item.trigger('drop');
	 	   	}
			})
			this.$el.droppable()
		}

		_.each(this.model.get("items").models, this.addDashboardItem, this)

		this.model.get("items").on("add", this.addDashboardItem, this)
		this.model.get("items").on("add", this.updateOrders, this)
		this.model.get("items").on("remove", this.removeDashboardItem, this)
		this.model.get("items").on("remove", this.updateOrders, this)
		this.model.get("items").on("orderchange", this.updateOrders,this)
		
		// Pass errors on items onto this view
		this.model.get("items").on("error", function(error, itemTitle) {
			_this.trigger('error', error, itemTitle)
		})

		$("body").on("classChange", function() {
			//This is called after the classChange
			//editing -> !editing
			if(!($("body").hasClass("editing"))) {
				if(!_this.model.saved){
					Streamr.showInfo('The dashboard has changes which are not saved', "Not saved")
				}
				_this.disableSortable()
			//!editing -> editing
			} else {
				_this.enableSortable()
			}
		})
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
	},

	updateOrders: function() {
		_.each(this.dashboardItemViews, function(item) {
				item.model.set("ord", item.$el.index())
		},this)
	},

	enableSortable: function() {
		if (this.$el.sortable) {
			this.$el.sortable("option", "disabled", false)
		}
	},

	disableSortable: function() {
		if (this.$el.sortable) {
			this.$el.sortable("option", "disabled", true)	
		}
	}
})

exports.Dashboard = Dashboard
exports.DashboardView = DashboardView
exports.SidebarView = SidebarView

})(typeof(exports) !== 'undefined' ? exports : window)