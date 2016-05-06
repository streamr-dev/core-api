package com.unifina.signalpath.statistics

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class KurtosisSpec extends Specification {

	Kurtosis module
	
    def setup() {
		module = new Kurtosis()
		module.init()
		module.configure([inputs: [
				[name: "windowLength", value: "100"],
				[name: "windowType", value: "EVENTS"],
				[name: "minSamples", value: "4"]
		]])
    }
	
	void "kurtosis gives the right answer"() {
		when:
		Map inputValues = [
			in: [1, 3, 1.5, 6, 7, -4, 0, 31].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [null, null, null, 0.97607072, -2.6135706, 0.34848284, -0.04397045, 5.74163759].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
