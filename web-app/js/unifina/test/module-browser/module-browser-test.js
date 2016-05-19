var assert = require('assert')
var fs = require('fs')
var jsdom = require("jsdom")
var window = jsdom.jsdom().defaultView
var $ = require('jquery')(window);
var _ = require('underscore')

var moduleTree
var getHelp
var mb

describe('module-browser-page', function(){
	before(function(){
		global.$ = $
		global.window = window
		global.document = window.document
		global.jQuery = $
		global._ = _
		$.fn.scrollspy = function(options){}
		global.Streamr = {
			showInfo: function(){},
			showError: function(){},
			showSuccess: function(){}
		}

		global.MathJax = {
			Hub: {
				Queue: function(param){
					if (typeof(param) === "function")
						param()
				}
			}
		}

		global.Streamr = {
			createLink: function(controller, action, id) {
				if (controller.uri)
					return controller.uri
				else if (id === undefined)
					return controller+"/"+action
				else
					return controller+"/"+action+"/"+id
			}
		}


		global.spinnerImg = ""

		mb = require('../../module-browser/module-browser')

		assert.equal($("body").length, 1)
	})

	beforeEach(function(){
		getHelp = function(id){
			var help = {
				helpText: "helpText-"+id,
				inputNames: ["inputName1-"+id, "inputName2-"+id],
				inputs: {},
				outputNames: ["outputName1-"+id],
				outputs: {},
				paramNames: [],
				params: {}
			}
			help.inputs["inputName1-"+id] = "inputHelp1-"+id
			help.inputs["inputName2-"+id] = "inputHelp2-"+id
			help.outputs["outputName1-"+id] = "outputHelp1-"+id
			return help
		}
		$("body").append("<div class='sidebar'></div><div class='module-tree'></div><input class='search'></input>")
	})

	describe('module-browser', function(){
		describe('rendering the structure', function(){
			beforeEach(function(){
				moduleTree = [
					{
						data: "module1",
						metadata: {id: 1}
					},{
						data: "module2",
						metadata: {id: 2},
						children: [{
							data: "submodule11",
							metadata: {id:4}
						}]
					},{
						data: "module3",
						metadata: {id: 3},
						children: [{
							data: "submodule12",
							metadata: {id: 5},
							children: [{
								data: "submodule21",
								metadata: {id: 6}
							}]
						}]
					}
				]
				$.getJSON = function(url, data, cb){
					if(url.split("/")[1] == "jsonGetModuleTree?modulesFirst=true"){
						cb(moduleTree)
					} else if(url.split("/")[1] == "canEdit"){
						cb({success: false})
					}
				}
				var moduleBrowser = new mb.ModuleBrowser({
					sidebarEl: ".sidebar",
					moduleTreeEl: ".module-tree",
					searchBoxEl: ".search"
				})
			})

			it('should have rendered the modules correctly with categories', function(){
				assert.equal($(".module-tree h2").length, 2)
				assert.equal($(".module-tree h4").length, 1)
				assert.equal($(".module-tree h2").eq(1).text(), "module3")
				assert.equal($(".module-tree h4").eq(0).text(), "module3 > submodule12")

				assert.equal($(".module-tree .panel").length, 3)
				assert.equal($(".module-tree .panel .panel-heading span").eq(0).text(), "module1")
				assert.equal($(".module-tree .panel .panel-heading span").eq(1).text(), "submodule11")
				assert.equal($(".module-tree .panel .panel-heading span").eq(2).text(), "submodule21")
			})
		})
		
		describe('rendering the helps', function() {
			var helpRegex = /api\/v1\/modules\/(.*)\/help/

			beforeEach(function(){
				moduleTree = [
					{
						data: "module1",
						metadata: {id: 11}
					},{
						data: "module2",
						metadata: {id: 22}
					}
				]
			})

			describe('without rights to edit', function(){
				beforeEach(function() {

					$.getJSON = function(url, data, cb){
						if (url.split("/")[1] == "jsonGetModuleTree?modulesFirst=true"){
							cb(moduleTree)
						} else if (url.split("/")[1] == "jsonGetModule"){
							cb({})
						} else if (url.match(helpRegex)) {
							var id = url.match(helpRegex)[1]
							cb(getHelp(id))
						} else {
							console.log("getJSON mock: I don't know how to respond to url "+url)
						}
					}
					var moduleBrowser = new mb.ModuleBrowser({
						sidebarEl: ".sidebar",
						moduleTreeEl: ".module-tree",
						searchBoxEl: ".search"
					})
					$("div.panel-collapse").trigger("show.bs.collapse")
				})

				it('should not have the "Edit" button rendered', function(){
					assert.notEqual($(".help-text-table .help-text").length, 0)
					assert.equal($(".module-tree .panel .btn").length, 0)
				})

				it('should have rendered the helps', function(){
					var panelBody = $(".module-tree .panel .panel-body").eq(1)
					assert.equal(panelBody.find(".help-text-table .help-text").text(), "helpText-22")
					assert.equal(panelBody.find(".input-table tbody tr").eq(0).find("td").eq(1).text(), "inputName1-22")
					assert.equal(panelBody.find(".input-table tbody tr").eq(0).find("td").eq(2).text(), "inputHelp1-22")
					assert.equal(panelBody.find(".input-table tbody tr").eq(1).find("td").eq(1).text(), "inputName2-22")
					assert.equal(panelBody.find(".input-table tbody tr").eq(1).find("td").eq(2).text(), "inputHelp2-22")
					assert.equal(panelBody.find(".output-table tbody tr").eq(0).find("td").eq(1).text(), "outputName1-22")
					assert.equal(panelBody.find(".output-table tbody tr").eq(0).find("td").eq(2).text(), "outputHelp1-22")
					assert.equal(panelBody.find(".param-table thead th").eq(0).text(), "No Parameter Helps")
				})
			})

			describe('editing the help', function(){
				beforeEach(function(){
					$.getJSON = function(url, data, cb){
						if(url.split("/")[1] == "jsonGetModuleTree?modulesFirst=true"){
							cb(moduleTree)
						} else if (url.match(helpRegex)) {
							var id = url.match(helpRegex)[1]
							cb(getHelp(id))
						} else if(url.split("/")[1] == "jsonGetModule"){
							cb({})
						} else if(url.split("/")[1] == "canEdit"){
							cb({success: true})
						}
					}
					var moduleBrowser = new mb.ModuleBrowser({
						sidebarEl: ".sidebar",
						moduleTreeEl: ".module-tree",
						searchBoxEl: ".search"
					})
					$("div.panel-collapse").trigger("show.bs.collapse")
				})

				afterEach(function(){
					window.CKEDITOR = undefined
					global.CKEDITOR = undefined
				})

				it('have rendered the editing buttons', function(){
					var panelBody = $(".module-tree .panel .panel-body").eq(1)
					assert.equal(panelBody.find(".edit-btn").length, 1)
					assert.equal(panelBody.find(".save-btn").length, 1)
					assert.equal(panelBody.find(".cancel-btn").length, 1)
				})

				it('should change the help-table fields to inputs and create CKEDITOR when clicked "Edit Help"', function(){
					var replaced = false
					$.ajax = function(attr){
						if(attr.url && attr.url == '//cdn.ckeditor.com/4.5.1/standard-all/ckeditor.js'){
							window.CKEDITOR = {
								replace: function(textareaId, options){
									replaced = true
									assert.equal(textareaId, "textarea22")
								}
							}
							global.CKEDITORConfigUrl = ""
							global.CKEDITOR = window.CKEDITOR
							attr.success()
						}
					}
					var panelBody = $(".module-tree .panel .panel-body").eq(1)
					panelBody.find(".edit-btn").click()

					assert.equal(panelBody.find(".help-text-table textarea.module-help").length, 1)
					assert.equal(panelBody.find(".help-text-table textarea.module-help").text(), "helpText-22")
					assert.equal(panelBody.find(".input-table tbody tr").eq(1).find("td input").val(), "inputHelp2-22")
					assert.equal(panelBody.find(".output-table tbody tr").eq(0).find("td input").val(), "outputHelp1-22")
					assert(replaced)
				})

				describe('saving', function(done){
					it('should save correctly', function(done){
						var panelBody = $(".module-tree .panel .panel-body").eq(0)
						var getNewHelp = function(id){
							var help = {
								helpText: "newHelpText-"+id,
								inputNames: ["inputName1-"+id, "inputName2-"+id],
								inputs: {},
								outputNames: ["outputName1-"+id],
								outputs: {},
								paramNames: [],
								params: {}
							}
							help.inputs["inputName1-"+id] = "inputHelp1-"+id
							help.inputs["inputName2-"+id] = "NEWInputHelp2-"+id
							help.outputs["outputName1-"+id] = "outputHelp1-"+id
							return help
						}
						var replaced = false
						var saved = false
						$.ajax = function(attr){
							if(attr.url && attr.url === '//cdn.ckeditor.com/4.5.1/standard-all/ckeditor.js'){
								window.CKEDITOR = {
									replace:  function(textareaId, options){
										replaced = true
										assert.equal(textareaId, "textarea11")
									},
									instances: {
										textarea11: {
											getData: function(){
												saved = true;
											}
										}
									}
								}
								global.CKEDITORConfigUrl = ""
								global.CKEDITOR = window.CKEDITOR
								attr.success()
							} else {
								assert.equal(attr.data.id, 11)
								assert.equal(JSON.toString(attr.data.jsonHelp), JSON.toString(getNewHelp(11)))
								assert(replaced)
								assert(saved)
								done()
							}
						}
						panelBody.find(".edit-btn").click()

						panelBody.find(".help-text-table textarea.module-help").text("newHelpText-11")
						panelBody.find(".input-table tbody tr").eq(1).find("td input").val("NEWInputHelp2-11")
						panelBody.find(".output-table tbody tr").eq(0).find("td input").val("outputHelp1-11")
						panelBody.find(".save-btn").click()
					})
				})
			})

			describe('if jsonModuleHelp and jsonModule have different data', function(){
				beforeEach(function(){
					$.getJSON = function(url, data, cb){
						if(url.split("/")[1] == "jsonGetModuleTree?modulesFirst=true"){
							cb(moduleTree)
						} else if(url.split("/")[1] == "jsonGetModule"){
							cb({inputs:[{name:"moduleExtraInput"}]})
						} else if (url.match(helpRegex)) {
							var id = url.match(helpRegex)[1]
							var help = getHelp(id)
							help.inputNames.push("helpExtraInput")
							cb(help)
						}
					}
					var moduleBrowser = new mb.ModuleBrowser({
						sidebarEl: ".sidebar",
						moduleTreeEl: ".module-tree",
						searchBoxEl: ".search"
					})
					$("div.panel-collapse").trigger("show.bs.collapse")
				})
				it('should have rendered all the inputs from both jsons', function(){
					var panelBody = $(".module-tree .panel .panel-body").eq(1)
					var names = []
					panelBody.find(".input-table tbody tr").each(function(i, el){
						names.push($(el).find("td").eq(1).text())
					})
					assert(names.indexOf("moduleExtraInput") > -1)
					assert(names.indexOf("helpExtraInput") > -1)
				})
			})
			
			describe('ajax queries answer in different orders', function(){
				afterEach(function(){
					it('should have rendered the helps', function(){
						var panelBody = $(".module-tree .panel .panel-body").eq(1)
						assert.equal(panelBody.find(".help-text-table .help-text").text(), "helpText-22")
						assert.equal(panelBody.find(".input-table tbody tr").eq(0).find("td").eq(1).text(), "inputName1-22")
						assert.equal(panelBody.find(".input-table tbody tr").eq(0).find("td").eq(2).text(), "inputHelp1-22")
						assert.equal(panelBody.find(".input-table tbody tr").eq(1).find("td").eq(1).text(), "inputName2-22")
						assert.equal(panelBody.find(".input-table tbody tr").eq(1).find("td").eq(2).text(), "inputHelp2-22")
						assert.equal(panelBody.find(".output-table tbody tr").eq(0).find("td").eq(1).text(), "outputName1-22")
						assert.equal(panelBody.find(".output-table tbody tr").eq(0).find("td").eq(2).text(), "outputHelp1-22")
						assert.equal(panelBody.find(".param-table thead th").eq(0).text(), "No Parameter Helps")
					})
				})
				it('should render normally when help comes first', function(){
					$.getJSON = function(url, data, cb){
						if(url.split("/")[1] == "jsonGetModuleTree?modulesFirst=true"){
							cb(moduleTree)
						} else if(url.split("/")[1] == "jsonGetModule"){
							setTimeout(function(){
								cb({})
							}, 50)
						} else if(url.split("/")[1] == "jsonGetModuleHelp"){
							cb(getHelp(url.split("/")[2]))
						}
					}
					var moduleBrowser = new mb.ModuleBrowser({
						sidebarEl: ".sidebar",
						moduleTreeEl: ".module-tree",
						searchBoxEl: ".search"
					})
					$("div.panel-collapse").trigger("show.bs.collapse")
					// afterEach
				})
				it('should render normally when module comes first', function(){
					$.getJSON = function(url, data, cb){
						if(url.split("/")[1] == "jsonGetModuleTree?modulesFirst=true"){
							cb(moduleTree)
						} else if(url.split("/")[1] == "jsonGetModule"){
							cb({})
						} else if(url.split("/")[1] == "jsonGetModuleHelp"){
							setTimeout(function(){
								cb(getHelp(url.split("/")[2]))
							}, 50)
						}
					}
					var moduleBrowser = new mb.ModuleBrowser({
						sidebarEl: ".sidebar",
						moduleTreeEl: ".module-tree",
						searchBoxEl: ".search"
					})
					$("div.panel-collapse").trigger("show.bs.collapse")
					// afterEach
				})
			})
		})
		describe('search', function(){
			var search = function(keyword){
				$(".search").val(keyword)
				$(".search").trigger("keyup")
			}
			beforeEach(function(){
				moduleTree = [
					{
						data: "amodule1",
						metadata: {id: 1}
					},{
						data: "abmodule2",
						metadata: {id: 2},
						children: [{
							data: "abcsubmodule11",
							metadata: {id:4}
						}]
					},{
						data: "bmodule3",
						metadata: {id: 3},
						children: [{
							data: "bbsubmodule12",
							metadata: {id: 5},
							children: [{
								data: "bcsubmodule21",
								metadata: {id: 6}
							}]
						}]
					}
				]
				$.getJSON = function(url, data, cb){
					if(url.split("/")[1] == "jsonGetModuleTree?modulesFirst=true"){
						cb(moduleTree)
					} else if(url.split("/")[1] == "canEdit"){
						cb({success: false})
					}
				}
				var moduleBrowser = new mb.ModuleBrowser({
					sidebarEl: ".sidebar",
					moduleTreeEl: ".module-tree",
					searchBoxEl: ".search"
				})
			})
			it('should open the right panel and close the last one when searched', function(done){
				var firstSearch = false
				$("#module6")[0].scrollIntoView = function(){
					setTimeout(function(){
						assert.equal($("#module6").find(".collapse.in").length, $(".collapse.in").length, 1)
					},50)
					firstSearch = true
				}
				search("bc")
				$("#module4")[0].scrollIntoView = function(){
					setTimeout(function(){
						assert.equal($("#module4").find(".collapse.in").length, $(".collapse.in").length, 1)
					},50)
					assert(firstSearch)
					done()
				}
				search("abc")
			})
			it('should close all the panels when searched empty string', function(){
				$(".collapse").addClass(".in")
				search("")
				assert.equal($(".collapse.in").length, 0)
			})
		})
	})

	describe('sidebar', function(){
		beforeEach(function(){
			moduleTree = [
				{
					data: "module1",
					metadata: {id: 1}
				},{
					data: "module2",
					metadata: {id: 2},
					children: [{
						data: "submodule11",
						metadata: {id:4}
					}]
				},{
					data: "module3",
					metadata: {id: 3},
					children: [{
						data: "submodule12",
						metadata: {id: 5},
						children: [{
							data: "submodule21",
							metadata: {id: 6}
						}]
					}]
				}
			]
			new mb.Sidebar(".sidebar", moduleTree)
		})

		it('should have rendered the sidebar correctly with sublevels', function(){
			assert.equal($("nav.streamr-sidebar").length, 1)
			assert.equal($("nav.streamr-sidebar ul.nav").length, 4)
			assert.equal($("nav.streamr-sidebar>.nav>li").length, 3)
			assert.equal($("nav.streamr-sidebar>.nav>li>.nav>li").length, 2)
			assert.equal($("nav.streamr-sidebar>.nav>li>.nav>li>.nav>li").length, 1)
		})
	})

	afterEach(function(){
		$("body").empty()
	})

	after(function(){
		global.$ = undefined
		global.window = undefined
		global.document = undefined
		global.jQuery = undefined
		global._ = undefined
		global.MathJax = undefined
		global.Streamr = undefined
		global.spinnerImg = undefined
		mb = undefined
	})
})