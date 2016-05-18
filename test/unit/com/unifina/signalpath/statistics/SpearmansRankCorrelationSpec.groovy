package com.unifina.signalpath.statistics

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class SpearmansRankCorrelationSpec extends Specification {

	SpearmansRankCorrelation module

	def setup() {
		module = new SpearmansRankCorrelation()
		module.init()
		module.configure([inputs: [
				[name: "windowLength", value: "3"],
				[name: "windowType", value: "EVENTS"],
				[name: "minSamples", value: "3"]
		]])
	}

	void "spearmansRankCorrelation gives the right answer"() {
		when:
		Map inputValues = [
			inX: [9, 15, 4, -666, -12, 0.25, 1].collect {it?.doubleValue()},
			inY: [-3, 2, 33, 0, 12, 100, -3].collect {it?.doubleValue()},
		]
		Map outputValues = [
			corr: [null, null, -0.5, 0.5, 1, 1, -0.5].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
