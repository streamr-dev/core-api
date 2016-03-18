package com.unifina.signalpath.simplemath

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class SumSpec extends Specification {

	Sum module

	def setup() {
		module = new Sum()
		module.init()
		module.configure([:])
	}

	void "sum without window gives the right answer"() {
		when:
		Map inputValues = [
				in: [3, 15, 10, -5, -20, 0, 5].collect { it?.doubleValue() }
		]
		Map outputValues = [
				"out": [3, 18, 28, 23, 3, 3, 8].collect { it?.doubleValue() }
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "sum with window gives the right answer"() {
		when:
		module.getInput("windowLength").receive(3);
		module.getInput("minSamples").receive(2);
		Map inputValues = [
				in: [3, 15, 10, -5, -20, 0, 5].collect { it?.doubleValue() }
		]
		Map outputValues = [
				"out": [null, 18, 28, 20, -15, -25, -15].collect { it?.doubleValue() }
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "sum with infinite window"() {
		when:
		module.getInput("windowLength").receive(0);
		module.getInput("minSamples").receive(1);
		Map inputValues = [
				in: (1..1000).collect { 1D }
		]
		Map outputValues = [
				"out": (1..1000).collect { it?.doubleValue() }
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}