package com.unifina.signalpath.filtering

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class MovingAverageModuleSpec extends Specification {
	
	MovingAverageModule module
	
    def setup() {
		module = new MovingAverageModule()
		module.init()
		module.configure(module.getConfiguration())
    }

	void "moving average is calculated correctly"() {
		module.getInput("windowLength").receive(2)
		module.getInput("minSamples").receive(1)
		
		when:
		Map inputValues = [
			in: [1,2,3,4,5,6]*.doubleValue()
		]
		Map outputValues = [
			out: [1, 1.5, 2.5, 3.5, 4.5, 5.5]*.doubleValue()
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
	
	void "moving average produces no output before minSamples samples is reached"() {
		module.getInput("windowLength").receive(3)
		module.getInput("minSamples").receive(3)
		
		when:
		Map inputValues = [
			in: [1,2,3,4]*.doubleValue()
		]
		Map outputValues = [
			out: [null, null, 2, 3].collect { it?.doubleValue() }
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
	
}
