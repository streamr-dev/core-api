package com.unifina.signalpath.trigger

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class ZeroCrossSpec extends Specification {

	ZeroCross module

	def setup() {
		module = new ZeroCross()
		module.init()
		module.configure([:])
	}

	void "zeroCross gives the right answer"() {
		when:
		Map inputValues = [
			in: [0, 0.2, -0.2, 0.6, -0.6, 1, 0.6, 1.6, 0.5, 3, -1, -0.5, -0.8, -666].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [null, null, -1, 1, -1, 1, 1, 1, 1, 1, -1, -1, -1, -1].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
