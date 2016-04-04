package com.unifina.signalpath.utils

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class MapAsTableSpec extends Specification {
	MapAsTable module

	def setup() {
		module = new MapAsTable()
		module.init()
		module.configure([
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
			.uiChannelMessages(channelMessages)
			.test()
	}

}
