package com.unifina.signalpath.modeling

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class ARIMASpec extends Specification {
	
	ARIMA module
	
    def setup() {
		module = new ARIMA()
		module.init()
		module.configure([
		    options: [
		        "AR(p)": [value: 2],
				"I(d)": [value: 4],
				"MA(q)": [value: 2],
		    ]
		])
    }

	void "ARIMA gives the right answer"() {
		when:
		Map inputValues = [
			in: [-0.4, 0.2, 0.8, 0.3, 0.5, 2, -2, 32, -0.4, 0.2, 0.8].collect {it?.doubleValue()},
		]
		Map outputValues = [
			// TODO: Disclaimer: I have no idea whether these numbers are expected out or not.
			pred: [null, 5.4, -18.3, 147.5, -203.6, 133.2, -31.6].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.skip(4)
			.test()
	}
}
