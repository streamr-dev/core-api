package com.unifina.signalpath.utils

import com.unifina.UiChannelMockingSpecification
import com.unifina.domain.User
import com.unifina.signalpath.SignalPath
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
@Mock([User])
class MapAsTableSpec extends UiChannelMockingSpecification {
	MapAsTable module

	def setup() {
		mockServicesForUiChannels()
		User user = new User(username: 'user').save(failOnError: true, validate: false)
		module = setupModule(new MapAsTable(), [
			uiChannel: [id: "table"],
		], new SignalPath(true), mockGlobals([:], user))
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
