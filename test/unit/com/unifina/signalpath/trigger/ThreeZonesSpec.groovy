package com.unifina.signalpath.trigger

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class ThreeZonesSpec extends Specification {

	ThreeZones module

	def setup() {
		module = new ThreeZones()
		module.init()
		module.configure([
			params: [
				[name: "highZone", value: 1], [name: "lowZone", value: -1],
			]
		])
	}

	void "threeZones gives the right answer"() {
		when:
		Map inputValues = [
			in: [0, 0.2, -0.2, 0.6, -0.6, 1, 0.6, 1.6, 0.5, 3, -1, -0.5, -0.8, -666].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [0, 0, 0, 0, 0, 1, 0, 1, 0, 1, -1, 0, 0, -1].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
