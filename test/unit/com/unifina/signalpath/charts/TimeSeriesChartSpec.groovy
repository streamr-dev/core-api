package com.unifina.signalpath.charts

import com.unifina.utils.Globals
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import com.unifina.signalpath.ModuleSpecification

@TestMixin(GrailsUnitTestMixin)
class TimeSeriesChartSpec extends ModuleSpecification {

	TimeSeriesChart module

	def setup() {
		module = new TimeSeriesChart()
		module.globals = new Globals()
		module.configure([
			uiChannel: [id: "timeSeries"],
			options: [
			    inputs: [value: 3],
				overnightBreak: [value: false]
			],
			barify: false
		])
	}

	void "timeSeriesChart (csv off) sends correct data to uiChannel"() {
		module.init()
		when:
		Map inputValues = [
			in1: [0,         null, null, null, 0.125, 0.05, null, null].collect {it?.doubleValue()},
			in2: [null, -3.141592, null, null,  null, null,  1.0,  180].collect {it?.doubleValue()},
			in3: [null,      null,  666,   42,  null, null, null, null].collect {it?.doubleValue()}
		]
		Map outputValues = [:]
		Map channelMessages = [
			timeSeries: [
				[
					type: "init",
					series: [
						[name: "outputForin1", idx: 0, step: true, yAxis: 0],
						[name: "outputForin2", idx: 1, step: true, yAxis: 0],
						[name: "outputForin3", idx: 2, step: true, yAxis: 0],
					]
				],
				new PointMessage(0, 0, 0.0),
				new PointMessage(1, 1000, -3.141592),
				new PointMessage(2, 2000, 666),
				new PointMessage(2, 3000, 42),
				new PointMessage(0, 4000, 0.125),
				new PointMessage(0, 5000, 0.05),
				new PointMessage(1, 6000, 1.0),
				new PointMessage(1, 7000, 180),
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.timeToFurtherPerIteration(1000)
			.uiChannelMessages(channelMessages)
			.overrideGlobals { g ->
				g.init()
				g.time = new Date(0)
				g
			}
			.test()
	}

	void "timeSeriesChart (csv on) sends correct data to uiChannel"() {
		module.globals.grailsApplication = grailsApplication
		module.globals.signalPathContext.put("csv", true)
		module.init()
		when:
		Map inputValues = [
			in1: [0,         null, null, null, 0.125, 0.05, null, null].collect {it?.doubleValue()},
			in2: [null, -3.141592, null, null,  null, null,  1.0,  180].collect {it?.doubleValue()},
			in3: [null,      null,  666,   42,  null, null, null, null].collect {it?.doubleValue()}
		]
		Map outputValues = [:]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.timeToFurtherPerIteration(1000)
			.overrideGlobals { g ->
				g.init()
				g.time = new Date(0)
				g.grailsApplication = grailsApplication
				g
			}
			.test()
		// TODO: test output values
	}
}
