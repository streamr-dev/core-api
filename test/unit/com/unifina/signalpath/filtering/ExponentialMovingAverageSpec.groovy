package com.unifina.signalpath.filtering

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class ExponentialMovingAverageSpec extends Specification {

	ExponentialMovingAverage module

    def setup() {
		module = new ExponentialMovingAverage()
		module.init()
    }

	void "exponentialMovingAverage must be calculated correctly"() {
		module.getInput("length").receive(2)
		module.getInput("minSamples").receive(1)

		when:
		Map inputValues = [
			in: [1,2,3,4,5,6].collect {it.doubleValue()}
		]
		Map outputValues = [
			out: [1, 1.5, 2.5, 3.5, 4.5, 5.5].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "must produce no output before minSamples samples"() {
		module.getInput("length").receive(3)
		module.getInput("minSamples").receive(3)

		when:
		Map inputValues = [
			in: [1,2,3,4].collect {it.doubleValue()}
		]
		Map outputValues = [
			out: [null, null, 2, 3].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

}
