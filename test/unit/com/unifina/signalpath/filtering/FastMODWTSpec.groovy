package com.unifina.signalpath.filtering

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class FastMODWTSpec extends Specification {

	FastMODWT module

    def setup() {
		module = new FastMODWT()
		module.init()
    }

	void "fastMODWT must be calculated correctly"() {
		when:
		module.getInput("level").receive(2)
		module.getInput("wavelet").receive("d4")
		Map inputValues = [
			in: (1..14).collect {it.doubleValue()}
		]
		Map outputValues = [
			smooth: (1..9).collect({ null }) + [8.09807621, 9.09807621, 10.09807621, 11.09807621, 12.09807621].collect {it?.doubleValue()},
			details: (1..9).collect({ null }) + [1.25, -0.52019482, 0.45828323, 0.48496342, 0.25390625].collect {it?.doubleValue()},
			energy: (1..9).collect({ null }) + [0, 0, 0, 0, 0].collect {it?.doubleValue()},
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
