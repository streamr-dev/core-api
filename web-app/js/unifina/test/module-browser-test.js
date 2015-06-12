var assert = require('assert')
var fs = require('fs')
var jsdom = require("jsdom")

var window = jsdom.jsdom().parentWindow
var $ = require('jquery')(window)

global.$ = $
global.window = window
global.document = window.document
global.jQuery = $

$.fn.scrollspy = function(options){}

var mb = require('../module-browser/module-browser')

assert.equal($("body").length, 1)

var moduleTree
var getHelp = function(id){
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

describe('module-browser', function(){

	beforeEach(function(){
		$("body").empty()
		$("body").append("<div class='sidebar'></div><div class='module-tree'></div><input class='search'></input>")
	})

	describe('rendering the structure', function(){
		
		beforeEach(function(done){
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
				if(url.split("/")[1] == "jsonGetModuleTree"){
					cb(moduleTree)
				} else if(url.split("/")[1] == "canEdit"){
					cb({success: false})
				}
			}
			var moduleBrowser = new mb.ModuleBrowser({
				url: 'url',
				sidebarEl: ".sidebar",
				moduleTreeEl: ".module-tree",
				searchBoxEl: ".search"
			})
			setTimeout(function(){
				done()
			},0)
		})
		it('should have rendered the sidebar correctly with sublevels', function(){
			assert.equal($("nav.streamr-sidebar").length, 1)
			assert.equal($("nav.streamr-sidebar ul.nav").length, 4)
			assert.equal($("nav.streamr-sidebar>.nav>li").length, 3)
			assert.equal($("nav.streamr-sidebar>.nav>li>.nav>li").length, 2)
			assert.equal($("nav.streamr-sidebar>.nav>li>.nav>li>.nav>li").length, 1)
		})

		it('should have rendered the modules correctly with categories', function(){
			assert.equal($(".module-tree h3").length, 3)
			assert.equal($(".module-tree h3").eq(0).text(), "module2")
			assert.equal($(".module-tree h3").eq(1).text(), "module3")
			assert.equal($(".module-tree h3").eq(2).text(), "submodule12")

			assert.equal($(".module-tree .panel").length, 3)
			assert.equal($(".module-tree .panel .panel-heading span").eq(0).text(), "module1")
			assert.equal($(".module-tree .panel .panel-heading span").eq(1).text(), "submodule11")
			assert.equal($(".module-tree .panel .panel-heading span").eq(2).text(), "submodule21")
		})

		it('should not have the "Edit" button rendered', function(){
			assert.equal($(".module-tree .panel .btn").length, 0)
		})
	})
	
	describe('rendering the helps', function(){
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
			beforeEach(function(done){
				$.getJSON = function(url, data, cb){
					if(url.split("/")[1] == "jsonGetModuleTree"){
						cb(moduleTree)
					} else if(url.split("/")[1] == "jsonGetModuleHelp"){
						cb(getHelp(url.split("/")[2]))
					}
				}
				var moduleBrowser = new mb.ModuleBrowser({
					url: 'url',
					sidebarEl: ".sidebar",
					moduleTreeEl: ".module-tree",
					searchBoxEl: ".search"
				})
				setTimeout(function(){
					done()
				},0)
			})

			it('should not have the "Edit" button rendered', function(){
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
			beforeEach(function(done){
				$.getJSON = function(url, data, cb){
					if(url.split("/")[1] == "jsonGetModuleTree"){
						cb(moduleTree)
					} else if(url.split("/")[1] == "jsonGetModuleHelp"){
						cb(getHelp(url.split("/")[2]))
					} else if(url.split("/")[1] == "canEdit"){
						cb({success: true})
					}
				}
				var moduleBrowser = new mb.ModuleBrowser({
					url: 'url',
					sidebarEl: ".sidebar",
					moduleTreeEl: ".module-tree",
					searchBoxEl: ".search"
				})
				setTimeout(function(){
					done()
				},0)
			})

			it('have rendered the editing buttons', function(){
				var panelBody = $(".module-tree .panel .panel-body").eq(1)
				assert.equal(panelBody.find(".edit-btn").length, 1)
				assert.equal(panelBody.find(".save-btn").length, 1)
			})

			it('should change the help-table fields to inputs when clicked "Edit Help"', function(){
				var panelBody = $(".module-tree .panel .panel-body").eq(1)
				panelBody.find(".edit-btn").click()
				assert.equal(panelBody.find(".help-text-table textarea.module-help").length, 1)
				assert.equal(panelBody.find(".help-text-table textarea.module-help").text(), "helpText-22")
				assert.equal(panelBody.find(".input-table tbody tr").eq(1).find("td input").val(), "<span>inputHelp2-22</span>")
				assert.equal(panelBody.find(".output-table tbody tr").eq(0).find("td input").val(), "<span>outputHelp1-22</span>")
			})

			describe('saving', function(){
				it('should save correctly', function(){
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
					$.ajax = function(attr){
						assert.equal(attr.data.id, 11)
						assert.equal(JSON.toString(attr.data.jsonHelp), JSON.toString(getNewHelp(11)))
					}
					panelBody.find(".edit-btn").click()

					panelBody.find(".help-text-table textarea.module-help").text("newHelpText-11")
					panelBody.find(".input-table tbody tr").eq(1).find("td input").val("<span>NEWInputHelp2-11</span>")
					panelBody.find(".output-table tbody tr").eq(0).find("td input").val("<span>outputHelp1-11</span>")
					panelBody.find(".save-btn").click()
				})
			})
		})
	})
})