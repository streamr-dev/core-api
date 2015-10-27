package com.unifina.signalpath.statistics

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class CovarianceSpec extends Specification {
	
	Covariance module
	
    def setup() {
		module = new Covariance()
		module.init()
    }

	void "covariance gives the right answer"() {
		when:
		module.getInput("windowLength").receive(4)
		Map inputValues = [
			inX: [1, 4, 0.5, 6, 3, 7, 10, 5].collect {it?.doubleValue()},
			inY: [2, 8, 1, 12, 6.15, 14.5, 21.5, 10].collect {it?.doubleValue()},
		]
		Map outputValues = [
			cov: [null, null, null, 13.45833333, 10.43958333, 17.88125, 18.325, 19.67083333].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
