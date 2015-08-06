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
	select.change()
}

var dateValidator
var scheduler

describe('dateValidator', function(){
	var startDate
	var endDate
	beforeEach(function(){
		dateValidator = s.dateValidator
	})
	describe('must return true', function(){
		afterEach(function(){
			assert(dateValidator.validate(startDate, endDate))
		})
		it('must return true when the startDate is clearly before the endDate', function(){
			startDate = {
				month: 1,
				day: 1,
				hour: 1,
				minute: 1
			}
			endDate = {
				month: 2,
				day: 2,
				hour: 2,
				minute: 2
			}
		})
		it('must return true when only minute is smaller in startDate', function(){
			startDate = {
				month: 1,
				day: 1,
				hour: 1,
				minute: 1
			}
			endDate = {
				month: 1,
				day: 1,
				hour: 1,
				minute: 2
			}
		})
		it('must return true when the startDate month is smaller but the day is bigger than in the endDate', function(){
			startDate = {
				month: 1,
				day: 3
			}
			endDate = {
				month: 2,
				day: 2
			}
		})
		it('must return true with only minute given correctly', function(){
			startDate = {
				minute: 1
			}
			endDate = {
				minute: 2
			}
		})
		it('must return true with the exactly same dates', function(){
			startDate = {
				month: 1,
				day: 1,
				hour: 1,
				minute: 1
			}
			endDate = {
				month: 1,
				day: 1,
				hour: 1,
				minute: 1
			}
		})
	})
	describe('must return false', function(){
		afterEach(function(){
			assert.equal(dateValidator.validate(startDate, endDate), false)
		})
		it('must return false when the startDate is clearly after the endDate', function(){
			startDate = {
				month: 2,
				day: 2,
				hour: 2,
				minute: 2
			}
			endDate = {
				month: 1,
				day: 1,
				hour: 1,
				minute: 1
			}
		})
		it('must return false when only minute is bigger in startDate', function(){
			startDate = {
				month: 1,
				day: 1,
				hour: 1,
				minute: 2
			}
			endDate = {
				month: 1,
				day: 1,
				hour: 1,
				minute: 1
			}
		})
		it('must return false when the startDate month is bigger but the day is smaller than in the endDate', function(){
			startDate = {
				month: 2,
				day: 3
			}
			endDate = {
				month: 1,
				day: 4
			}
		})
		it('must return false with only minute given not correctly', function(){
			startDate = {
				minute: 2
			}
			endDate = {
				minute: 1
			}
		})
	})
})

describe('scheduler', function() {
	beforeEach(function(){
		$("body").append($("<div id='scheduler'></div>"))
		$("body").append(templates)
	})
	afterEach(function(){
		$("body").empty()
	})

	describe('rendering', function(){
		beforeEach(function(){
			scheduler = new s.Scheduler({
				el: "#scheduler"
			})
		})
		it('must have the "hour" option selected first', function(){
			assert.equal($("ol.table li.range").length, 1)
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
		it('must have the add-range-button', function(){
			assert.equal($(".btn.add-range-btn").length, 1)
		})
	})

	describe('changing interval-type', function(){
		beforeEach(function(){
			scheduler = new s.Scheduler({
				el: "#scheduler"
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

	describe('adding and removing ranges', function(){
		beforeEach(function(){
			scheduler = new s.Scheduler({
				el: "#scheduler"
			})
		})
		it('must add the ranges when clicked add-button', function(){
			assert.equal($("ol.table li.range").length, 1)
			$(".btn.add-range-btn").click()
			assert.equal($("ol.table li.range").length, 2)
			$(".btn.add-range-btn").click()
			assert.equal($("ol.table li.range").length, 3)
		})
		it('must remove the ranges when clicked delete-button', function(){
			$(".btn.add-range-btn").click()
			$(".btn.add-range-btn").click()
			$("ol.table li.range").eq(0).find(".delete-btn").click()
			assert.equal($("ol.table li.range").length, 2)
			$("ol.table li.range").find(".delete-btn").click()
			assert.equal($("ol.table li.range").length, 0)
		})
		it('must remove the ranges when clicked delete-button', function(){
			$(".btn.add-range-btn").click()
			$(".btn.add-range-btn").click()
			$("ol.table li.range").eq(0).find(".delete-btn").click()
			assert.equal($("ol.table li.range").length, 2)
			$("ol.table li.range").find(".delete-btn").click()
			assert.equal($("ol.table li.range").length, 0)
		})
	})

	describe('reordering ranges', function(){
		beforeEach(function(){
			scheduler = new s.Scheduler({
				el: "#scheduler"
			})
			$(".btn.add-range-btn").click()
			$(".btn.add-range-btn").click()
			$("ol.table li.range").eq(0).find("select[name='interval-type']").val(4)
			$("ol.table li.range").eq(2).find("select[name='interval-type']").val(1)
			$("ol.table li.range").eq(0).find("input[name='value']").val("10")
			$("ol.table li.range").eq(2).find("input[name='value']").val("20")
		})
		it('must change the order when clicked move-down-btn', function(){
			$("ol.table li.range").eq(0).find(".move-down-btn").click()
			assert.equal($("ol.table li.range").eq(0).find("select[name='interval-type']").val(), 0)
			assert.equal($("ol.table li.range").eq(1).find("select[name='interval-type']").val(), 4)
			$("ol.table li.range").eq(1).find(".move-down-btn").click()
			assert.equal($("ol.table li.range").eq(1).find("input[name='value']").val(), "20")
			assert.equal($("ol.table li.range").eq(2).find("input[name='value']").val(), "10")
		})
		it('must change the order when clicked move-up-btn', function(){
			$("ol.table li.range").eq(2).find(".move-up-btn").click()
			assert.equal($("ol.table li.range").eq(1).find("select[name='interval-type']").val(), 1)
			assert.equal($("ol.table li.range").eq(2).find("select[name='interval-type']").val(), 0)
			$("ol.table li.range").eq(1).find(".move-up-btn").click()
			assert.equal($("ol.table li.range").eq(0).find("input[name='value']").val(), "20")
			assert.equal($("ol.table li.range").eq(1).find("input[name='value']").val(), "10")
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
				el: "#scheduler"
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
		it('Should trigger Error if a range value is not given', function(){
			$("ol.table li.range").eq(0).find("input[name='value']").val("")
			$("ol.table li.range").eq(0).find("input[name='value']").keyup()
			shouldFail(function(){
				scheduler.buildJSON()
			}, "Ranges must have a value!")
			assert(hasGoneToError && hasFailed)
		})
		it('Should trigger Error if the startDate is after the endDate', function(done){
			$("ol.table li.range").eq(0).find(".startDate select[name='minute']").val(20)
			$("ol.table li.range").eq(0).find(".startDate select[name='minute']").change()
			$("ol.table li.range").eq(0).find(".endDate select[name='minute']").val(0)
			$("ol.table li.range").eq(0).find(".endDate select[name='minute']").change()
			shouldFail(function(){
				scheduler.buildJSON()
			}, "The end date must be after the start date!", done)
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
		var range1
		var range2
		beforeEach(function(){
			scheduler = new s.Scheduler({
				el: "#scheduler"
			})
			$(".btn.add-range-btn").click()
			$(".default-value input[name='default-value']").val(100)

			range1 = $("li.range").eq(0)
			range1.find("select[name='interval-type']").val(2)
			range1.find("select[name='interval-type']").change()
			range1.find(".startDate select[name='weekday']").val(0)
			range1.find(".startDate select[name='weekday']").change()
			range1.find(".endDate select[name='weekday']").val(2)
			range1.find(".endDate select[name='weekday']").change()
			range1.find("input[name='value']").val("10")
			range1.find("input[name='value']").keyup()

			range2 = $("li.range").eq(1)
			range2.find("select[name='interval-type']").val(1)
			range2.find("select[name='interval-type']").change()
			range2.find(".startDate select[name='hour']").val(10)
			range2.find(".startDate select[name='hour']").change()
			range2.find(".startDate select[name='minute']").val(10)
			range2.find(".startDate select[name='minute']").change()
			range2.find(".endDate select[name='hour']").val(12)
			range2.find(".endDate select[name='hour']").change()
			range2.find(".endDate select[name='minute']").val(20)
			range2.find(".endDate select[name='minute']").change()
			range2.find("input[name='value']").val("20")
			range2.find("input[name='value']").keyup()

			testJSON = {
				defaultValue: 100,
				ranges: [{
					intervalType: 2,
					startDate: {
						weekday: 0,
						hour: 0,
						minute: 0
					},
					endDate: {
						weekday: 2,
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
			range1.find(".startDate select[name='weekday']").val(1)
			range1.find(".startDate select[name='weekday']").change()
			testJSON.ranges[0].startDate.weekday = 1

			assert.deepEqual(scheduler.buildJSON(), testJSON)
		})

		it('should reorder the ranges in JSON when the fields are reordered', function(){
			range1.find(".move-down-btn").click()
			var newRanges = []
			newRanges.push(testJSON.ranges[1])
			newRanges.push(testJSON.ranges[0])
			testJSON.ranges = newRanges

			assert.deepEqual(scheduler.buildJSON(), testJSON)
		})
	})

	describe('rendering from a config file', function(){
		beforeEach(function(){
			var config = {
				defaultValue: 3,
				ranges: [{
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
						weekday: 0,
						hour: 10,
						minute: 25
					},
					endDate: {
						weekday: 2,
						hour: 11,
						minute: 35
					}
				}]
			}
			scheduler = new s.Scheduler({
				el: "#scheduler",
				config: config
			})
		})
		it('must have rendered all the ranges', function(){
			assert.equal($("ol.table li.range").length, 2)
		})
		it('must have the correct values in the first range', function(){
			var range = $("ol.table li.range").eq(0)
			assert.equal(range.find("select[name='interval-type'] option:selected").text(), "day")

			assert.equal(range.find(".startDate select[name='hour']").val(), 10)
			assert.equal(range.find(".startDate select[name='minute']").val(), 25)

			assert.equal(range.find(".endDate select[name='hour']").val(), 11)
			assert.equal(range.find(".endDate select[name='minute']").val(), 35)

			assert.equal(range.find("input[name='value']").val(), 100)
		})
		it('must have the correct values in the second range', function(){
			var range = $("ol.table li.range").eq(1)
			assert.equal(range.find("select[name='interval-type'] option:selected").text(), "week")

			assert.equal(range.find(".startDate select[name='weekday'] option:selected").text().toLowerCase(), "monday")
			assert.equal(range.find(".startDate select[name='hour']").val(), 10)
			assert.equal(range.find(".startDate select[name='minute']").val(), 25)

			assert.equal(range.find(".endDate select[name='weekday'] option:selected").text().toLowerCase(), "wednesday")
			assert.equal(range.find(".endDate select[name='hour']").val(), 11)
			assert.equal(range.find(".endDate select[name='minute']").val(), 35)

			assert.equal(range.find("input[name='value']").val(), 200)
			assert.equal($(".default-value input").val(), 3)
		})
	})
})