var assert = require('assert')
var fs = require('fs')
var jsdom = require("jsdom")
var window = jsdom.jsdom().defaultView
var $ = require('jquery')(window);
var _ = require('underscore')
var Backbone = require('backbone-associations')

var sidebar
var dashboard
var dashboardView
var dashboardJson
var canvases
var templates
var db

describe('dashboard-editor', function() {
	before(function(){
		global.$ = $
		global._ = _
		global.window = window
		global.document = window.document
		global.Backbone = Backbone
		Backbone.$ = $
		global.jQuery = $
		global.Streamr = {
			showInfo: function(){},
			showError: function(){},
			showSuccess: function(){}
		}
		// jsdom wants to use the deprecated style of creating events, so let's mock a bridge between old and new
		global.Event = function(type) {
			var e = window.document.createEvent("CustomEvent")
			e.initEvent(type, true, true)
			return e
		}

		templates = fs.readFileSync('grails-app/views/dashboard/_dashboard-template.gsp','utf8') // read content of _dashboard-template.gsp
		assert.equal($("body").length, 1)
		$("body").append(templates)
		db = require('../../dashboard/dashboard-editor')

		global.ConfirmButton = function(options) {
			return options
		}
		
	})

	beforeEach(function() {
		$("body").append(templates)
		$("body").append("<ul id='dashboard-view'></ul>")
		$("body").append("<div id='sidebar-view'><button id='main-menu-toggle'></button></div>")

		dashboardJson = {
			id: 1,
			name: "Test",
			items: [
				{id: 1, title: "Item1", ord:0, canvas: "canvas-1", module: 1, webcomponent: 'streamr-chart', size:"medium"}, //chart
				{id: 2, title: "Item2", ord:1, canvas: "canvas-3", module: 1, webcomponent: 'streamr-label', size:"small"}  //label
			]
		}

		canvases = [
			{id: "canvas-1", state: 'running', name: "RSP1", modules: [
				{hash: 1, uiChannel: {name: "uiChannel-1", checked: true, id: "uiChannel-id-1", webcomponent: "streamr-chart"}}
				]},
			{id: "canvas-2", state: 'running', name: "RSP2", modules: [
				{hash: 1, uiChannel: {name: "uiChannel-3", checked: false, id: "uiChannel-id-3", webcomponent: "streamr-label"}},
				{hash: 2, uiChannel: {name: "uiChannel-4", checked: false, id: "uiChannel-id-4", webcomponent: "streamr-heatmap"}}
				]},
			{id: "canvas-3", state: 'stopped', name: "RSP3", modules: [
				{hash: 1, uiChannel: {name: "uiChannel-2", checked: true, id: "uiChannel-id-2", webcomponent: "streamr-label"}}
			]},			
			{id: "canvas-4", state: 'running', name: "RSP4", modules: [
				{hash: 1, uiChannel: {name: "uiChannel-5", checked: false, id: "uiChannel-id-5", webcomponent: "streamr-chart"}}
				]}
		]
	})

	afterEach(function() {
		dashboard = undefined
		$("body").empty()
	})

	after(function(){
		global.$ = undefined
		global._ = undefined
		global.window = undefined
		global.document = undefined
		global.Backbone = undefined
		Backbone.$ = undefined
		global.jQuery = undefined
		global.Streamr = undefined
		global.Event = undefined

		templates = undefined
		db = undefined

		global.Toolbar = undefined
	})

	describe('with all permissions', function() {
		var super_toJSON
		before(function() {
			super_toJSON = global.$.toJSON
		})
		beforeEach(function () {
			global.$.getJSON = function(url, cb) {
				// Does not contain permission/me
				if (url.indexOf("permissions/me") < 0) {
					super_getJSON(url, cb)
				} else {
					cb([{
						operation: "read"
					},{
						operation: "write"
					},{
						operation: "share"
					}])
				}
			}
			dashboard = new db.Dashboard(dashboardJson)
			dashboard.urlRoot = "nourl"

			dashboardView = new db.DashboardView({
				model: dashboard,
				el: $("#dashboard-view"),
				baseUrl: "/"
			})

			sidebar = new db.SidebarView({
				el: $("#sidebar-view"),
				dashboard: dashboard,
				canvases: canvases,
				menuToggle: $("#main-menu-toggle"),
				baseUrl: "/"
			})
		})
		describe("Dashboard", function() {
			it('should return the same JSON representation it was initialized with', function () {
				assert.deepEqual(dashboard.toJSON(), dashboardJson)
			})

			it('should throw error when trying to create module without webcomponent', function () {
				assert.throws(
					function () {
						dashboard.get("items").push({
							title: "Item3",
							canvas: 'canvas-2',
							module: 1,
							webcomponent: undefined,
							size: "medium"
						})
					}, Error);
			})

			it('should render the amount of dashboardItems correctly when items are added', function () {
				assert.equal($(".streamr-widget").length, 2)
				assert.equal(dashboardView.$el.children().length, 2)
				dashboard.get("items").push({
					title: "Item3",
					canvas: "canvas-2",
					module: 1,
					webcomponent: "streamr-chart",
					size: "medium"
				})
				assert.equal($(".streamr-widget").length, 3)
				assert.equal(dashboardView.$el.children().length, 3)
			})

			it('must remove a dashboarditem when clicked delete', function () {
				assert.equal(dashboardView.$el.children().length, 2)
				$(dashboardView.$el.children()[0]).find(".btn.delete-btn").click()
				assert.equal(dashboardView.$el.children().length, 1)
				$(dashboardView.$el.children()[0]).find(".btn.delete-btn").click()
				assert(!dashboardView.$el.children().length)
			})

			it('must remove a dashboarditem when it is removed from collection', function () {
				assert.equal(dashboardView.$el.children().length, 2)
				var item = dashboard.get("items").models[1]
				dashboard.get("items").remove(item)
				assert.equal(dashboardView.$el.children().length, 1)
			})

			it('must change the order when indexes changed', function () {
				assert.equal($(dashboardView.$el.children()[0]).index(), 0)
				assert.equal($(dashboardView.$el.children()[1]).index(), 1)
				assert.equal(dashboard.get("items").models[0].get("ord"), 0)
				assert.equal(dashboard.get("items").models[1].get("ord"), 1)

				//Simulate jQuery's sortable, which triggers 'drop'-event when dragged to another place
				$(dashboardView.$el.children()[0]).insertAfter(dashboardView.$el.children()[1])
				$(dashboardView.$el.children()[0]).trigger('drop')

				assert.equal($(dashboardView.$el.children()[0]).index(), 0)
				assert.equal($(dashboardView.$el.children()[1]).index(), 1)
				//Ords changed
				assert.equal(dashboard.get("items").models[0].get("ord"), 1)
				assert.equal(dashboard.get("items").models[1].get("ord"), 0)
			})

			it('must change the name of a dashboarditem from input', function () {
				//dashboarditem doesn't have class'editing' (titlebar-edit shouldn't be visible)
				assert(!($(dashboardView.$el.children()[0]).hasClass("editing")))

				$(dashboardView.$el.children()[0]).find(".btn.edit-btn").click()

				//dashboarditem has class 'editing' (titlebar-edit should turn visible)
				assert($(dashboardView.$el.children()[0]).hasClass("editing"))

				$(dashboardView.$el.children()[0]).find(".name-input").val("test-name")
				$(dashboardView.$el.children()[0]).find(".btn.close-edit").click()
				//should change the dashboarditem's title
				assert.equal(dashboard.get("items").models[0].get("title"), "test-name")
			})

			it('must also edit the title by clicking titlebar and blurring', function () {
				//dashboarditem doesn't have class'editing' (titlebar-edit shouldn't be visible)
				assert(!($(dashboardView.$el.children()[0]).hasClass("editing")))

				$(dashboardView.$el.children()[0]).find(".titlebar-clickable").click()

				//dashboarditem has class 'editing' (titlebar-edit should turn visible)
				assert($(dashboardView.$el.children()[0]).hasClass("editing"))

				$(dashboardView.$el.children()[0]).find(".name-input").val("test-name")
				$($(dashboardView.$el.children()[0]).find(".name-input")).trigger("focusout")
				assert(!($(dashboardView.$el.children()[0]).hasClass("editing")))
				//should change the dashboarditem's title
				assert.equal(dashboard.get("items").models[0].get("title"), "test-name")
			})

			it('must also work with enter', function () {
				//dashboarditem doesn't have class'editing' (titlebar-edit shouldn't be visible)
				assert(!($(dashboardView.$el.children()[0]).hasClass("editing")))

				$(dashboardView.$el.children()[0]).find(".btn.edit-btn").click()

				assert($(dashboardView.$el.children()[0]).hasClass("editing"))

				$(dashboardView.$el.children()[0]).find(".name-input").click()
				var press = $.Event("keypress", {keyCode: 13})
				$(dashboardView.$el.children()[0]).find(".name-input").trigger(press)
				assert(!($(dashboardView.$el.children()[0]).hasClass("editing")))
			})

			it('must change the size from the dropdown', function () {
				assert(!($(dashboardView.$el.children()[0]).hasClass("editing")))

				$(dashboardView.$el.children()[0]).click()
				assert($($(dashboardView.$el.children()[0])).hasClass("medium-size"))

				$(dashboardView.$el.children()[0]).find(".dropdown-toggle").click()
				$(dashboardView.$el.children()[0]).find(".make-large-btn").click()
				assert(!($($(dashboardView.$el.children()[0])).hasClass("medium-size")))
				assert($(dashboardView.$el.children()[0]).hasClass("large-size"))

				$(dashboardView.$el.children()[0]).find(".dropdown-toggle").click()
				$(dashboardView.$el.children()[0]).find(".make-small-btn").click()
				assert(!($($(dashboardView.$el.children()[0])).hasClass("large-size")))
				assert($(dashboardView.$el.children()[0]).hasClass("small-size"))

				$(dashboardView.$el.children()[0]).find(".dropdown-toggle").click()
				$(dashboardView.$el.children()[0]).find(".make-medium-btn").click()
				assert(!($($(dashboardView.$el.children()[0])).hasClass("small-size")))
				assert($(dashboardView.$el.children()[0]).hasClass("medium-size"))
			})
		})
		describe("Sidebar", function() {
			it('must render runningsignalpath-elements', function() {
				assert.equal($("#sidebar-view").find(".canvas").length, 4)
			})

			it("must render all runningsignalpaths closed", function () {
				_.each($(".canvas"), function(rsp){
					assert(!$(rsp).hasClass("open"))
				})
			})

			it('must add class checked to every checked module', function () {
				var i = 0
				_.each(sidebar.canvases.models, function (canvas){
					var j = 0
					_.each(canvas.get('modules').models, function(module){
						if(module.get("checked")){
							assert($($($("#sidebar-view").find(".canvas")[i]).find(".module")[j]).hasClass("checked"))
						}
						else
							assert(!($($($("#sidebar-view").find(".canvas")[i]).find(".module")[j]).hasClass("checked")))
						j++
					},this)
					i++
				},this)
			})

			it('must open the rsp when clicked', function () {
				assert(!($($("#sidebar-view").find(".canvas")[1]).hasClass("open")))
				$($("#sidebar-view").find(".canvas .canvas-title")[1]).click()
				assert($($("#sidebar-view").find(".canvas")[1]).hasClass("open"))
			})

			it('must check clicked modules', function () {
				//Wait for 'checked' event
				var checked = false
				sidebar.$el.on("checked", function (){
					checked = true
				})
				//First canvas is closed
				assert(!($($("#sidebar-view").find(".canvas")[1]).hasClass("open")))
				//Clicked to open
				$($("#sidebar-view").find(".canvas .canvas-title")[1]).click()

				//First module and its element are not checked
				assert(!($($($("#sidebar-view").find(".canvas")[1]).find(".uichannel")[0]).hasClass("checked")))
				assert(!(sidebar.canvases.models[1].get('modules').models[0].get("checked")))

				//Click on module title
				$($($("#sidebar-view").find(".canvas")[1]).find(".module .module-title")[0]).click()

				//module must get class checked
				assert($($($("#sidebar-view").find(".canvas")[1]).find(".module")[0]).hasClass("checked"))

				//module element turns checked
				assert(sidebar.canvases.models[1].get('modules').models[0].get("checked"))

				//Check that 'checked'-event was triggered
				assert(checked)
			})

			it('must uncheck clicked uiChannels', function () {
				//Wait for 'unchecked' event
				var unchecked = false
				sidebar.$el.on("unchecked", function (){
					unchecked = true
				})

				//First runningsignalpath is closed
				assert(!($($("#sidebar-view").find(".canvas")[0]).hasClass("open")))
				//Clicked to open
				$($("#sidebar-view").find(".canvas .canvas-title")[0]).click()

				//First uiChannel and uichannel element are not checked
				assert($($($("#sidebar-view").find(".canvas")[0]).find(".module")[0]).hasClass("checked"))
				assert(sidebar.canvases.models[0].get('modules').models[0].get("checked"))
				//Unchecked
				$($($("#sidebar-view").find(".canvas")[0]).find(".module .module-title")[0]).click()

				//Class checked of the uichannel must be removed
				assert(!($($($("#sidebar-view").find(".canvas")[0]).find(".module")[0]).hasClass("checked")))

				//uiChannel element turns unchecked
				assert(!(sidebar.canvases.models[0].get('modules').models[0].get("checked")))

				//Check that 'unchecked'-ecent has got triggered
				assert(unchecked)
			})

			it('must change the name of the dashboard in name-input with clicking save', function () {
				assert.equal(dashboard.get("name"), "Test")
				$("input.dashboard-name").val("changed-name")

				//click save button
				$("#sidebar-view .save-button").click()

				//Dashboard name should have changed
				assert.equal(dashboard.get("name"), "changed-name")
			})

			it('must trigger "resize"-event for all dashboarditems when menuToggle is clicked', function (done) {
				assert.equal($(".streamr-widget").length, 2)
				assert.equal(dashboardView.$el.children().length, 2)

				var counter = 0
				$(".streamr-widget").on('resize', function() {
					counter++

					if (counter===2)
						done()
				})

				sidebar.menuToggle.click()
			})

			it('must add the class "stopped" for the runningsignalpaths which have state:stopped', function() {
				assert.equal($("#sidebar-view").find(".canvas").length, 4)
				assert(!($($("#sidebar-view").find(".canvas")[0]).hasClass("stopped")))
				assert(!($($("#sidebar-view").find(".canvas")[1]).hasClass("stopped")))
				assert($($("#sidebar-view").find(".canvas")[2]).hasClass("stopped"))
				assert(!($($("#sidebar-view").find(".canvas")[4]).hasClass("stopped")))
			})

		})

		describe("save button", function() {
			it("must call backbone model save function", function(done) {
				dashboard.save = function() {
					done()
				}
				$("#sidebar-view .save-button").click()
			})
		})
	})

	describe('with limited permissions', function() {
		var super_toJSON
		before(function() {
			super_toJSON = global.$.toJSON
		})
		afterEach(function() {
			global.$.toJSON = super_toJSON
		})
		describe('Sidebar', function() {

			it('must not enable shareButton without share-permission', function(done) {
				global.$.getJSON = function(url, cb) {
					// Does not contain permission/me
					if (url.indexOf("permissions/me") < 0) {
						super_getJSON(url, cb)
					} else {
						cb([{
							operation: "read"
						},{
							operation: "write"
						}])
						assert($("#share-button").attr("disabled"))
						done()
					}
				}
				dashboard = new db.Dashboard(dashboardJson)
				dashboard.urlRoot = "nourl"

				dashboardView = new db.DashboardView({
					model: dashboard,
					el: $("#dashboard-view"),
					baseUrl: "/"
				})

				sidebar = new db.SidebarView({
					el: $("#sidebar-view"),
					dashboard: dashboard,
					canvases: canvases,
					menuToggle: $("#main-menu-toggle"),
					baseUrl: "/"
				})
			})

			it('must not enable saveButton and deleteButton without write-permission', function(done) {
				global.$.getJSON = function(url, cb) {
					// Does not contain permission/me
					if (url.indexOf("permissions/me") < 0) {
						super_getJSON(url, cb)
					} else {
						cb([{
							operation: "read"
						}])
						assert($("#saveButton").attr("disabled"))
						assert($("#deleteDashboardButton").attr("disabled"))
						done()
					}
				}
				dashboard = new db.Dashboard(dashboardJson)
				dashboard.urlRoot = "nourl"

				dashboardView = new db.DashboardView({
					model: dashboard,
					el: $("#dashboard-view"),
					baseUrl: "/"
				})

				sidebar = new db.SidebarView({
					el: $("#sidebar-view"),
					dashboard: dashboard,
					canvases: canvases,
					menuToggle: $("#main-menu-toggle"),
					baseUrl: "/"
				})
			})
		})
	})
})