var assert = require('assert')
var fs = require('fs')
var jsdom = require("jsdom")

var window = jsdom.jsdom().defaultView
var $ = require('jquery')(window);
var _ = require('underscore')
var Backbone = require('../../backbone/backbone')

var select
var s
var templates
var scheduler

describe('scheduler', function() {
	before(function(){
		global.$ = $
		global._ = _
		// Normally done in streamr.js
		_.templateSettings = {
			evaluate : /\{\[([\s\S]+?)\]\}/g, // {[ ]}
			escape : /\[\[([\s\S]+?)\]\]/g, // [[ ]]
			interpolate : /\{\{([\s\S]+?)\}\}/g // {{ }}
		}
		global.window = window
		global.document = window.document
		global.Backbone = Backbone
		Backbone.$ = $
		global.jQuery = $

		assert.equal($("body").length, 1)

		s = require('../signalPath/specific/scheduler')

		select = function(selectName, optionValue, index){
			var row = $("li").eq(index || 0)
			var select = row.find("select[name='"+selectName+"']")
			select.val(optionValue)
			select.change()
		}
	})
	beforeEach(function(){
		$("body").append($("<div id='scheduler'></div><div id='footer'></div>"))
	})
	afterEach(function(){
		$("body").empty()
	})

	describe('rendering', function(){
		beforeEach(function(){
			scheduler = new s.Scheduler({
				el: "#scheduler",
				footerEl: "#footer"
			})
		})
		it('must have the "hour" option selected first', function(){
			assert.equal($("ol.table li.rule").length, 1)
			assert.equal($("div.td.date").length, 1)
			assert.equal($("select[name='interval-type'] option:selected").text(), "hour")
			assert.equal($("select[name='day']").length, 0)
			assert.equal($("select[name='minute']").length, 2)
			assert.equal($("div.startDate:contains('minutes')").length, 1)
			assert.equal($("div.endDate:contains('minutes')").length, 1)
		})
		it('must have the value field', function(){
			assert.equal($("div.td.value").length, 1)
		})
		it('must have the delete-button', function(){
			assert.equal($(".delete-btn").length, 1)
		})
		it('must have the default value field', function(){
			assert.equal($("input[name='default-value']").length, 1)
			assert.equal($("input[name='default-value']").val(), 0)
		})
		it('must have the add-rule-button', function(){
			assert.equal($(".btn.add-rule-btn").length, 1)
		})
	})

	describe('changing interval-type', function(){
		beforeEach(function(){
			scheduler = new s.Scheduler({
				el: "#scheduler",
				footerEl: "#footer"
			})
		})
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

	describe('adding and removing rules', function(){
		beforeEach(function(){
			scheduler = new s.Scheduler({
				el: "#scheduler",
				footerEl: "#footer"
			})
		})
		it('must add the rules when clicked add-button', function(){
			assert.equal($("ol.table li.rule").length, 1)
			$(".btn.add-rule-btn").click()
			assert.equal($("ol.table li.rule").length, 2)
			$(".btn.add-rule-btn").click()
			assert.equal($("ol.table li.rule").length, 3)
		})
		it('must remove the rules when clicked delete-button', function(){
			$(".btn.add-rule-btn").click()
			$(".btn.add-rule-btn").click()
			$("ol.table li.rule").eq(0).find(".delete-btn").click()
			assert.equal($("ol.table li.rule").length, 2)
			$("ol.table li.rule").find(".delete-btn").click()
			assert.equal($("ol.table li.rule").length, 0)
		})
		it('must remove the rules when clicked delete-button', function(){
			$(".btn.add-rule-btn").click()
			$(".btn.add-rule-btn").click()
			$("ol.table li.rule").eq(0).find(".delete-btn").click()
			assert.equal($("ol.table li.rule").length, 2)
			$("ol.table li.rule").find(".delete-btn").click()
			assert.equal($("ol.table li.rule").length, 0)
		})
	})

	describe('reordering rules', function(){
		beforeEach(function(){
			scheduler = new s.Scheduler({
				el: "#scheduler",
				footerEl: "#footer"
			})
			$(".btn.add-rule-btn").click()
			$(".btn.add-rule-btn").click()
			$("ol.table li.rule").eq(0).find("select[name='interval-type']").val(4)
			$("ol.table li.rule").eq(2).find("select[name='interval-type']").val(1)
			$("ol.table li.rule").eq(0).find("input[name='value']").val("10")
			$("ol.table li.rule").eq(2).find("input[name='value']").val("20")
		})
		it('must change the order when clicked move-down-btn', function(){
			$("ol.table li.rule").eq(0).find(".move-down-btn").click()
			assert.equal($("ol.table li.rule").eq(0).find("select[name='interval-type']").val(), 0)
			assert.equal($("ol.table li.rule").eq(1).find("select[name='interval-type']").val(), 4)
			$("ol.table li.rule").eq(1).find(".move-down-btn").click()
			assert.equal($("ol.table li.rule").eq(1).find("input[name='value']").val(), "20")
			assert.equal($("ol.table li.rule").eq(2).find("input[name='value']").val(), "10")
		})
		it('must change the order when clicked move-up-btn', function(){
			$("ol.table li.rule").eq(2).find(".move-up-btn").click()
			assert.equal($("ol.table li.rule").eq(1).find("select[name='interval-type']").val(), 1)
			assert.equal($("ol.table li.rule").eq(2).find("select[name='interval-type']").val(), 0)
			$("ol.table li.rule").eq(1).find(".move-up-btn").click()
			assert.equal($("ol.table li.rule").eq(0).find("input[name='value']").val(), "20")
			assert.equal($("ol.table li.rule").eq(1).find("input[name='value']").val(), "10")
		})
	})

	describe('validation', function(){
		var hasGoneToError
		var hasFailed
		var shouldFail
		beforeEach(function(){
			hasGoneToError = false
			hasFailed = false
			scheduler = new s.Scheduler({
				el: "#scheduler",
				footerEl: "#footer"
			})
			shouldFail = function(c, text, done){
				scheduler.on("Error", function(msg){
					hasGoneToError = true
					assert.equal(msg.text, text)
					if(done){
						done()
					}
				})
				try {
					c()
				} catch (e) {
					hasFailed = true
				}
			}
		})
		it('Should trigger Error if a rule value is not given', function(){
			$("ol.table li.rule").eq(0).find("input[name='value']").val("")
			$("ol.table li.rule").eq(0).find("input[name='value']").keyup()
			shouldFail(function(){
				scheduler.buildJSON()
			}, "Rules must have a value!")
			assert(hasGoneToError && hasFailed)
		})
		it('Should trigger Error if the scheduler does not have a default value', function(done){
			$("input[name='default-value']").val("")
			shouldFail(function(){
				scheduler.buildJSON()
			}, "The scheduler needs a default value!", done)
			assert(hasGoneToError && hasFailed)
		})
	})

	describe('printing the JSON', function(){
		var testJSON
		var rule1
		var rule2
		beforeEach(function(){
			scheduler = new s.Scheduler({
				el: "#scheduler",
				footerEl: "#footer"
			})
			$(".btn.add-rule-btn").click()
			$(".default-value input[name='default-value']").val(100)

			rule1 = $("li.rule").eq(0)
			rule1.find("select[name='interval-type']").val(2)
			rule1.find("select[name='interval-type']").change()
			rule1.find(".startDate select[name='weekday']").val(2)
			rule1.find(".startDate select[name='weekday']").change()
			rule1.find(".endDate select[name='weekday']").val(3)
			rule1.find(".endDate select[name='weekday']").change()
			rule1.find("input[name='value']").val("10")
			rule1.find("input[name='value']").keyup()

			rule2 = $("li.rule").eq(1)
			rule2.find("select[name='interval-type']").val(1)
			rule2.find("select[name='interval-type']").change()
			rule2.find(".startDate select[name='hour']").val(10)
			rule2.find(".startDate select[name='hour']").change()
			rule2.find(".startDate select[name='minute']").val(10)
			rule2.find(".startDate select[name='minute']").change()
			rule2.find(".endDate select[name='hour']").val(12)
			rule2.find(".endDate select[name='hour']").change()
			rule2.find(".endDate select[name='minute']").val(20)
			rule2.find(".endDate select[name='minute']").change()
			rule2.find("input[name='value']").val("20")
			rule2.find("input[name='value']").keyup()

			testJSON = {
				defaultValue: 100,
				rules: [{
					intervalType: 2,
					startDate: {
						weekday: 2,
						hour: 0,
						minute: 0
					},
					endDate: {
						weekday: 3,
						hour: 0,
						minute: 0
					},
					value: 10
				}, {
					intervalType: 1,
					startDate: {
						hour: 10,
						minute: 10
					},
					endDate: {
						hour: 12,
						minute: 20
					},
					value: 20
				}]
			}
		})

		it('should print the JSON correctly', function(){
			assert.deepEqual(scheduler.buildJSON(), testJSON)
		})

		it('should update the JSON correctly', function(){
			rule1.find(".endDate select[name='weekday']").val(1)
			rule1.find(".endDate select[name='weekday']").change()
			testJSON.rules[0].endDate.weekday = 1

			assert.deepEqual(scheduler.buildJSON(), testJSON)
		})

		it('should reorder the rules in JSON when the fields are reordered', function(){
			rule1.find(".move-down-btn").click()
			var newRules = []
			newRules.push(testJSON.rules[1])
			newRules.push(testJSON.rules[0])
			testJSON.rules = newRules

			assert.deepEqual(scheduler.buildJSON(), testJSON)
		})
	})

	describe('rendering from a config file', function(){
		beforeEach(function(){
			var schedule = {
				defaultValue: 3,
				rules: [{
					value: 100,
					intervalType: 1,
					startDate: {
						hour: 10,
						minute: 25
					},
					endDate: {
						hour: 11,
						minute: 35
					}
				}, {
					value: 200,
					intervalType: 2,
					startDate: {
						weekday: 2,
						hour: 10,
						minute: 25
					},
					endDate: {
						weekday: 3,
						hour: 11,
						minute: 35
					}
				}]
			}
			scheduler = new s.Scheduler({
				el: "#scheduler",
				schedule: schedule,
				footerEl: "#footer"
			})
		})
		it('must have rendered all the rules', function(){
			assert.equal($("ol.table li.rule").length, 2)
		})
		it('must have the correct values in the first rule', function(){
			var rule = $("ol.table li.rule").eq(0)
			assert.equal(rule.find("select[name='interval-type'] option:selected").text(), "day")

			assert.equal(rule.find(".startDate select[name='hour']").val(), 10)
			assert.equal(rule.find(".startDate select[name='minute']").val(), 25)

			assert.equal(rule.find(".endDate select[name='hour']").val(), 11)
			assert.equal(rule.find(".endDate select[name='minute']").val(), 35)

			assert.equal(rule.find("input[name='value']").val(), 100)
		})
		it('must have the correct values in the second rule', function(){
			var rule = $("ol.table li.rule").eq(1)
			assert.equal(rule.find("select[name='interval-type'] option:selected").text(), "week")

			assert.equal(rule.find(".startDate select[name='weekday'] option:selected").text().toLowerCase(), "monday")
			assert.equal(rule.find(".startDate select[name='hour']").val(), 10)
			assert.equal(rule.find(".startDate select[name='minute']").val(), 25)

			assert.equal(rule.find(".endDate select[name='weekday'] option:selected").text().toLowerCase(), "tuesday")
			assert.equal(rule.find(".endDate select[name='hour']").val(), 11)
			assert.equal(rule.find(".endDate select[name='minute']").val(), 35)

			assert.equal(rule.find("input[name='value']").val(), 200)
			assert.equal($(".default-value input").val(), 3)
		})
	})

	after(function(){
		global.$ = undefined
		global._ = undefined
		global.window = undefined
		global.document = undefined
		global.Backbone = undefined
		Backbone.$ = undefined
		global.jQuery = undefined
		s = undefined
		select = undefined
	})
})