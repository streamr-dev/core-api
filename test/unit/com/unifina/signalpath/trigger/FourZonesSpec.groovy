package com.unifina.signalpath.trigger

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class FourZonesSpec extends Specification {

	FourZones module

	def setup() {
		module = new FourZones()
		module.init()
		module.configure([
			params: [
				[name: "highTrigger", value: 1], [name: "highRelease", value: 0.5],
				[name: "lowTrigger", value: -1], [name: "lowRelease", value: -0.5],
			]
		])
	}

	void "fourZones (mode=enter) gives the right answer"() {
		when:
		module.getInput("mode").receive(1)
		Map inputValues = [
			in: [0, 0.2, -0.2, 0.6, -0.6, 1, 0.6, 1.6, 0.5, 3, -1, -0.5, -0.8, -666].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [0, 0, 0, 0, 0, 1, 1, 1, 0, 1, -1, 0, 0, -1].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "fourZones (mode=exit) gives the right answer"() {
		when:
		module.getInput("mode").receive(2)
		Map inputValues = [
			in: [0, 0.2, -0.2, 0.6, -0.6, 1, 0.6, 1.6, 0.5, 3, -1, -0.5, -0.8, -666].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
