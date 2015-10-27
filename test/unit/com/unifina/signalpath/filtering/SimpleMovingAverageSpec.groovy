package com.unifina.signalpath.filtering

import spock.lang.Specification

import com.unifina.utils.testutils.ModuleTestHelper

class SimpleMovingAverageSpec extends Specification {
	
	SimpleMovingAverageEvents sma
	
    def setup() {
		sma = new SimpleMovingAverageEvents()
		sma.init()
    }

    def cleanup() {
		
    }

	void "sma must be calculated correctly"() {
		sma.getInput("length").receive(2)
		sma.getInput("minSamples").receive(1)
		
		when:
		Map inputValues = [
			in: [1,2,3,4,5,6].collect {it.doubleValue()}
		]
		Map outputValues = [
			out: [1, 1.5, 2.5, 3.5, 4.5, 5.5].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(sma, inputValues, outputValues).test()
	}
	
	void "must produce no output before minSamples samples"() {
		sma.getInput("length").receive(3)
		sma.getInput("minSamples").receive(3)
		
		when:
		Map inputValues = [
			in: [1,2,3,4].collect {it.doubleValue()}
		]
		Map outputValues = [
			out: [null, null, 2, 3].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(sma, inputValues, outputValues).test()
	}
	
}
