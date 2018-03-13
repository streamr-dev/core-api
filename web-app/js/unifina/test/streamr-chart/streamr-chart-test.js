var assert = require('assert')
var $ = require('jquery')(require("jsdom").jsdom().defaultView);
var StreamrChart = require('../../streamr-chart/streamr-chart').StreamrChart

describe('streamr-chart', function() {
	var chart
	var $parent

	before(function() {
		global.$ = $
		global.window = {
			requestAnimationFrame: function(cb) {
				setTimeout(cb,0)
			}
		}
	})

	beforeEach(function() {
		$parent = $('<div></div>')
		chart = new StreamrChart($parent)

		global.Highcharts = {
			setOptions: function(options) {},
			StockChart: function(options) {
				var result = $.extend(options, {
					xAxis: [{
						getExtremes: function() {
							return {
								min: chart.minTime,
								max: chart.maxTime
							}
						},
						setExtremes: function(min, max) {}
				}],
					setSize: function(innerWidth, innerHeight) {},
					redraw: function() {}
				})

				options.series.forEach(function(series) {
					series.addPoint = function(arr) {

					}
				})

				return result
			}
		}
	})

	it('should append the chart draw area to parent on creation', function() {
		assert(chart.$area)
		assert($parent.find("#"+chart.$area.attr('id')).length)
	})

	describe('chart options', function() {
		it('should add range selector by default', function() {
			assert($parent.find(".chart-range-selector").length)
		})
		it('rangeDropdown: false should not add range selector', function() {
			$parent = $('<div></div>')
			chart = new StreamrChart($parent, {rangeDropdown:false})
			assert.equal($parent.find(".chart-range-selector").length, 0)
		})

		it('should add show/hide series buttons by default', function() {
			assert($parent.find(".chart-series-buttons").length)
		})
		it('showHideButtons: false should not add show/hide buttons', function() {
			$parent = $('<div></div>')
			chart = new StreamrChart($parent, {showHideButtons:false})
			assert.equal($parent.find(".chart-series-buttons").length, 0)
		})
	})

	describe('init message', function() {
		it('should establish the seriesMeta and yAxis objects', function() {
			chart.handleMessage({
				type: 'init',
				series: [{
					name: 'test',
					yAxis: 0,
					idx: 0
				},
				{
					name: 'test2',
					yAxis: 1,
					idx: 1
				}]
			})

			assert.equal(chart.seriesMeta.length, 2)
			assert.equal(chart.yAxis.length, 2)
		})

		it('should remap client-side yAxis values', function() {
			chart.handleMessage({
				type: 'init',
				series: [{
					name: 'test',
					yAxis: 0,
					idx: 0
				},
				{
					name: 'test2',
					yAxis: 3,
					idx: 1
				}]
			})

			assert.equal(chart.seriesMeta.length, 2)
			assert.equal(chart.yAxis.length, 2)
			assert.equal(chart.realYAxis[3], 1)
		})

		it('can be included in constructor options', function() {
			chart = new StreamrChart($parent, {
				init: {
					type: 'init',
					series: [{
						name: 'test',
						yAxis: 0,
						idx: 0
					},
					{
						name: 'test2',
						yAxis: 1,
						idx: 1
					}]
				}
			})

			assert.equal(chart.seriesMeta.length, 2)
			assert.equal(chart.yAxis.length, 2)
		})
	})

	describe('data point message', function() {
		beforeEach(function() {
			// Init
			chart.handleMessage({
				type: 'init',
				series: [{
					name: 'test',
					yAxis: 0,
					idx: 0
				},
				{
					name: 'test2',
					yAxis: 1,
					idx: 1
				}]
			})
		})

		it('should update minimums and maximums', function() {
			var now = Date.now()

			chart.handleMessage({
				type: 'p',
				x: now,
				y: 10,
				s: 0
			})

			assert.equal(chart.minTime, now)
			assert.equal(chart.maxTime, now)

			chart.handleMessage({
				type: 'p',
				x: now+1000,
				y: 11,
				s: 0
			})

			assert.equal(chart.minTime, now)
			assert.equal(chart.maxTime, now+1000)

			chart.handleMessage({
				type: 'p',
				x: now-1000,
				y: 12,
				s: 0
			})

			assert.equal(chart.minTime, now-1000)
			assert.equal(chart.maxTime, now+1000)

			assert.equal(chart.seriesMeta[0].min, 10)
			assert.equal(chart.seriesMeta[0].max, 12)

			assert(chart.latestNavigatorTimestamp != null)
		})

		it('should hold the first point in seriesMeta to avoid Highcharts bug, then create chart on second point', function() {
			var now = Date.now()

			chart.handleMessage({
				type: 'p',
				x: now,
				y: 10,
				s: 0
			})

			assert.equal(chart.chart, null)
			assert.equal(chart.seriesMeta[0].data.length, 1)

			chart.handleMessage({
				type: 'p',
				x: now+1000,
				y: 10,
				s: 0
			})

			assert(chart.chart != null)

			// The other series with no points must get two null points so that it can be drawn
			assert.equal(chart.seriesMeta[1].data.length, 2)
		})

		it('should add null to the other series when the other gets two points and it only has one', function() {
			var now = Date.now()

			chart.handleMessage({
				type: 'p',
				x: now,
				y: 10,
				s: 0
			})

			chart.handleMessage({
				type: 'p',
				x: now,
				y: 20,
				s: 1
			})

			chart.handleMessage({
				type: 'p',
				x: now+1000,
				y: 11,
				s: 0
			})

			assert.equal(chart.seriesMeta[1].data.length, 2)
			assert.equal(chart.seriesMeta[1].data[0][0], now)
			assert.equal(chart.seriesMeta[1].data[0][1], 20)
			assert.equal(chart.seriesMeta[1].data[1][0], now+1000)
			assert.equal(chart.seriesMeta[1].data[1][1], null)
		})

		it('should add two nulls to the other series when the other gets two points and it has none', function() {
			var now = Date.now()

			chart.handleMessage({
				type: 'p',
				x: now,
				y: 10,
				s: 0
			})

			chart.handleMessage({
				type: 'p',
				x: now+1000,
				y: 11,
				s: 0
			})

			assert.equal(chart.seriesMeta[1].data.length, 2)
			assert.equal(chart.seriesMeta[1].data[0][0], now)
			assert.equal(chart.seriesMeta[1].data[0][1], null)
			assert.equal(chart.seriesMeta[1].data[1][0], now+1000)
			assert.equal(chart.seriesMeta[1].data[1][1], null)
		})

		it('should trigger the updated event and redraw the chart', function(done) {
			var now = Date.now()

			for (var i=0; i<5; i++) {
				chart.handleMessage({
					type: 'p',
					x: now+i*1000,
					y: 10,
					s: 0
				})
			}

			chart.handleMessage({
				type: 'p',
				x: now+10000,
				y: 10,
				s: 0
			})

			// First message won't trigger redraw
			var redrawCount = 0
			chart.chart.redraw = function() {
				if (++redrawCount === 5) {
					done()
				}
			}

		})

	})

	describe('day break message', function() {
		it('should add a null value at the end of the series', function(done) {
			// Init
			chart.handleMessage({
				type: 'init',
				series: [{
					name: 'test',
					yAxis: 0,
					idx: 0
				}]
			})

			for (var i=0; i<5; i++) {
				chart.handleMessage({
					type: 'p',
					x: i,
					y: i,
					s: 0
				})
			}

			chart.chart.series[0].addPoint = function(data) {
				assert.equal(data[1], null)
				done()
			}

			chart.handleMessage({
				type: 'b',
				s: 0
			})
		})
	})

	describe('series message', function() {
		it('should add a new series to the chart', function(done) {
			// Init
			chart.handleMessage({
				type: 'init',
				series: [{
					name: 'test',
					yAxis: 0,
					idx: 0
				}]
			})
			// Some data
			for (var i=0; i<5; i++) {
				chart.handleMessage({
					type: 'p',
					x: i,
					y: i,
					s: 0
				})
			}

			chart.chart.addSeries = function(series) {
				throw "Series added too early!"
			}

			// Add another series to same yAxis
			chart.handleMessage({
				type: 's',
				series: {
					name: 'new',
					yAxis: 0,
					idx: 1
				}
			})

			assert.equal(chart.seriesMeta.length, 2)

			// Should not be added before getting the second data point
			chart.handleMessage({
				type: 'p',
				x: 10,
				y: 10,
				s: 1
			})
			chart.chart.addSeries = function(series) {
				assert.equal(series.data.length, 2)
				done()
			}
			chart.handleMessage({
				type: 'p',
				x: 10,
				y: 10,
				s: 1
			})

		})
	})

})