var assert = require('assert')
var $ = require('jquery')(require("jsdom").jsdom().parentWindow);
global.$ = $
var StreamrChart = require('../streamr-chart/streamr-chart').StreamrChart

global.window = {
	requestAnimationFrame: function(cb) {
		setTimeout(cb,0)
	}
}

describe('streamr-chart', function() {
	var chart
	var $parent

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

	describe('init message', function() {
		it('should establish the seriesMeta and yAxis objects', function() {
			chart.handleMessage({
				type: 'init',
				series: [{
					name: 'test',
					yAxis: 0
				},
				{
					name: 'test2',
					yAxis: 1
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
					yAxis: 0
				},
				{
					name: 'test2',
					yAxis: 3
				}]
			})

			assert.equal(chart.seriesMeta.length, 2)
			assert.equal(chart.yAxis.length, 2)
			assert.equal(chart.realYAxis[3], 1)
		})
	})

	describe('data point message', function() {
		beforeEach(function() {
			// Init
			chart.handleMessage({
				type: 'init',
				series: [{
					name: 'test',
					yAxis: 0
				},
				{
					name: 'test2',
					yAxis: 1
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
})