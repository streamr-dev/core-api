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


var templates = fs.readFileSync('web-app/js/unifina/test/scheduler-template.html','utf8')
assert.equal($("body").length, 1)

var s = require('../signalPath/specific/scheduler')

var select = function(selectName, optionValue, index){
	var row = $("li").eq(index || 0)
	var select = row.find("select[name='"+selectName+"']")
	select.val(optionValue)
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

	describe('rendering', function(){
		it('must have the "hour" option selected first', function(){
			assert.equal($("ol.table li").length, 1)
			assert.equal($("div.td.date").length, 1)
			assert.equal($("select[name='interval-type'] option:selected").text(), "hour")
			assert.equal($("select[name='day']").length, 0)
			assert.equal($("select[name='minute']").length, 2)
			assert.equal($("div.start:contains('minutes')").length, 1)
			assert.equal($("div.end:contains('minutes')").length, 1)
		})
		it('must have the value field', function(){
			assert.equal($("div.td.value").length, 1)
		})
		it('must have the delete-button', function(){
			assert.equal($(".delete-btn").length, 1)
		})
		it('must have the default value field', function(){
			assert.equal($("input[name='default']").length, 1)
			assert.equal($("input[name='default']").val(), 0)
		})
		it('must have the add-range-button', function(){
			assert.equal($(".btn.add-range-btn").length, 1)
		})
	})

	describe('changing options', function(){
		it('must change to the year option when selected', function(){
			select("interval-type", 4)
			assert.equal($("select[name='month']").length, 2)
			assert.equal($("select[name='day']").length, 2)
			assert.equal($("select[name='hour']").length, 2)
			assert.equal($("select[name='minute']").length, 2)
			assert.equal($("div:contains('minutes from the beginning of the hour')").length, 0)
		})
		it('must change to the month option when selected', function(){
			select("interval-type", 3)
			assert.equal($("select[name='month']").length, 0)
			assert.equal($("select[name='day']").length, 2)
			assert.equal($("select[name='hour']").length, 2)
			assert.equal($("select[name='minute']").length, 2)
		})
		it('must change to the week option when selected', function(){
			select("interval-type", 2)
			assert.equal($("select[name='month']").length, 0)
			assert.equal($("select[name='day']").length, 0)
			assert.equal($("select[name='weekday']").length, 2)
			assert.equal($("select[name='hour']").length, 2)
			assert.equal($("select[name='minute']").length, 2)
		})
		it('must change to the day option when selected', function(){
			select("interval-type", 1)
			assert.equal($("select[name='weekday']").length, 0)
			assert.equal($("select[name='hour']").length, 2)
			assert.equal($("select[name='minute']").length, 2)
		})
	})
})