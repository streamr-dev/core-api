package com.unifina.signalpath.text

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class FormatNumberSpec extends Specification {

	FormatNumber module

	def setup(){
		module = new FormatNumber()
		module.init()
		module.configure(module.getConfiguration())
	}

	void "FormatNumber works properly"() {
		when:
		Map inputValues = [
			decimalPlaces: [0, 2, 4, 4, -2],
			number: [Math.PI, Math.PI, Math.PI, 1d, 315486.2185]
		]
		Map outputValues = [
			text: ["3", "3.14", "3.1416", "1.0000", "315486"]
		]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
