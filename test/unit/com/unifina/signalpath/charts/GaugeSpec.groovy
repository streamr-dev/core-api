package com.unifina.signalpath.charts

import com.unifina.UiChannelMockingSpecification
import com.unifina.domain.security.SecUser
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin) // for grailsApplication
@Mock([SecUser])
class GaugeSpec extends UiChannelMockingSpecification {
	
	Gauge module
	
    def setup() {
		mockServicesForUiChannels()

		module = setupModule(new Gauge(), [
			uiChannel: [id: "gauge"],
			params: [
				[name: "min", value: -1],
				[name: "max", value: 1]
			],
			options: [
			  title: [value: "Gauge"]
			]
		])
    }

	void "gauge sends correct data to uiChannel"() {
		when:
		Map inputValues = [
			value: [0, 0.2, 0.9, 1.0, 1.5, -1 -0.5, -3, 0].collect {it?.doubleValue()},
		]
		Map outputValues = [:]
		Map channelMessages = [
				gauge: [
				[min: -1.0D, v: 0.0D, title: "Gauge", max: 1.0D, type: "init"],
				[v: 0.2D, type: "u"],
				[v: 0.9D, type: "u"],
				[v: 1.0D, type: "u"],
				[v: 1.5D, type: "u"],
				[v: -1.5D, type: "u"],
				[v: -3D, type: "u"],
				[v: 0D, type: "u"],
			]
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages, getSentMessagesByStreamId())
			.test()
	}
}
