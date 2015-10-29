package com.unifina.signalpath.trigger

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class PeakDetectSpec extends Specification {

	PeakDetect module

	def setup() {
		module = new PeakDetect()
		module.init()
		module.configure([
			params: [
				[name: "highZone", value: 1], [name: "lowZone", value: -1], [name: "threshold", value: 0]
			]
		])
	}

	void "peakDetect gives the right answer"() {
		when:
		Map inputValues = [
			in: [0, -0.15, -0.5, -3, -0.3, 0.6, 1.8, 4, -2].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [null, null, null, null, 1, 1, 1, 1, -1].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
