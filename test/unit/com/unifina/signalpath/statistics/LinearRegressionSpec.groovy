package com.unifina.signalpath.statistics

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class LinearRegressionSpec extends Specification {

	LinearRegression module

	def setup() {
		module = new LinearRegression()
		module.init()
		module.configure([inputs: [
				[name: "windowLength", value: "3"],
				[name: "windowType", value: "EVENTS"],
				[name: "minSamples", value: "3"]
		]])
	}

	void "linearRegression gives the right answer"() {
		when:
		Map inputValues = [
			in: [1, 2, 3, 4, 5.1, 5.9, 13].collect {it?.doubleValue()},
		]
		Map outputValues = [
			slope: [null, null, 1, 1, 1.05, 0.95, 3.95].collect {it?.doubleValue()},
			error: [null, null, 0, 0, 0.00166667, 0.015, 6.615].collect {it?.doubleValue()},
			"R^2": [null, null, 1, 1, 0.99924471, 0.99175824, 0.82509254].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
