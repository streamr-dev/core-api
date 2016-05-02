package com.unifina.signalpath.statistics

import com.unifina.utils.testutils.ModuleTestHelper
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic
import spock.lang.Specification

class GeometricMeanSpec extends Specification {

	GeometricMean module
	
    def setup() {
		module = new GeometricMean()
		module.init()
    }
	
	void "geometricMean gives the right answer"() {
		module.configure([inputs: [
				[name: "windowLength", value: "3"],
				[name: "windowType", value: "EVENTS"],
				[name: "minSamples", value: "2"]
		]])

		when:
		Map inputValues = [
			in: [1, 3, 1.5, 6, 7, 31, 8].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [null, 1.73205081, 1.65096362, 3, 3.97905721, 10.91952285, 12.01849001].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
