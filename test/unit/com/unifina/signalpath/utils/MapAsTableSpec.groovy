package com.unifina.signalpath.utils

import com.unifina.UiChannelMockingSpecification
import com.unifina.domain.security.SecUser
import com.unifina.utils.GlobalsFactory
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.support.GrailsUnitTestMixin

@Mixin(GrailsUnitTestMixin)
class MapAsTableSpec extends UiChannelMockingSpecification {
	MapAsTable module

	def setup() {
		mockServicesForUiChannels()
		module = setupModule(new MapAsTable(), [
			uiChannel: [id: "table"],
		])
	}

	def "MapAsTable works correctly"() {
		when:
		Map inputValues = [
		    map: [
		        [:],
				[hello: "world"],
				[a: 13, b: 666, c: 32, d: "d"]
		    ]
		]
		Map outputValues = [:]
		Map channelMessages = [
			table: [
				[hdr: [headers: ["key", "value"]]],
				[nm: [:]],
				[nm: [hello: "world"]],
				[nm: [a: 13, b: 666, c: 32, d: "d"]]
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages, getSentMessagesByStreamId())
			.test()
	}

}
