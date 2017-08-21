package com.unifina.signalpath.charts

import com.unifina.UiChannelMockingSpecification
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class TimeSeriesChartSpec extends UiChannelMockingSpecification {

	TimeSeriesChart module

	def setup() {
		mockServicesForUiChannels()
		module = setupModule(new TimeSeriesChart(), [
				uiChannel: [id: "timeSeries"],
				options: [
						inputs: [value: 3],
						overnightBreak: [value: false]
				],
				barify: false
		])
	}

	void "timeSeriesChart sends correct data to uiChannel"() {
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
					title: null,
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
			.uiChannelMessages(channelMessages, getSentMessagesByStreamId())
			.test()
	}
}
