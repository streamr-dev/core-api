package com.unifina.signalpath.utils

import com.unifina.signalpath.filtering.MovingAverageModule
import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class MovingWindowSpec extends Specification {

	MovingWindow module

	def setup() {
		module = new MovingWindow()
		module.init()
		module.configure(module.getConfiguration())
	}

	void "movingWindow works correctly"() {
		module.getInput("windowLength").receive(2)
		module.getInput("minSamples").receive(1)

		when:
		Map inputValues = [
			in: [1,2,3,4,5,6]
		]
		Map outputValues = [
			list: [[1], [1,2], [2,3], [3,4], [4,5], [5,6]]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "movingWindow produces no output before minSamples samples is reached"() {
		module.getInput("windowLength").receive(3)
		module.getInput("minSamples").receive(3)

		when:
		Map inputValues = [
			in: [1,2,3,4]
		]
		Map outputValues = [
			list: [null, null, [1, 2, 3], [2, 3, 4]]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

}
