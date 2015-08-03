var assert = require('assert')
var fs = require('fs')
var jsdom = require("jsdom")

var window = jsdom.jsdom().parentWindow
var $ = require('jquery')(window);
var _ = require('underscore')
var Backbone = require('../../backbone/backbone')

global.$ = $
global._ = _
global.window = window
global.document = window.document
global.Backbone = Backbone
Backbone.$ = $
global.jQuery = $


var templates = fs.readFileSync('web-app/js/unifina/signalPath/specific/schedulerModuleTemplate.html','utf8')
assert.equal($("body").length, 1)

var s = require('../signalPath/specific/scheduler')

var select = function(selectName, optionValue, trClass){
	var tr = $("tr"+ (trClass ? "."+trClass : ""))
	var select = tr.find("select[name='"+selectName+"']")
	select.find("option:selected").attr("selected", "")
	select.find("option[value='"+optionValue+"']").attr("selected", "selected")
	select.change()
}

var scheduler

describe('scheduler', function() {
	beforeEach(function(){
		$("body").append($("<div id='scheduler'></div>"))
		$("body").append(templates)
		scheduler = new s.Scheduler({
			el: "#scheduler"
		})
	})
	afterEach(function(){
		$("body").empty()
	})

	describe('rendering of the selects', function(){
		it('must have the "hour" option selected first', function(){
			assert.equal($("table tbody tr").length, 2)
			assert.equal($("tr.start").length, 1)
			assert.equal($("tr.end").length, 1)
			assert.equal($("select[name='interval-type'] option:selected").text(), "hour")
			assert.equal($("select[name='day']").length, 0)
			assert.equal($("select[name='minute']").length, 2)
			assert.equal($("td:contains('minutes from the beginning of the hour')").length, 2)
		})
		it('must change to the year option when selected', function(){
			select("interval-type", 4, "start")
			assert.equal($("select[name='month']").length, 2)
			assert.equal($("select[name='day']").length, 2)
			assert.equal($("td:contains('minutes from the beginning of the hour')").length, 0)
		})
	})

})