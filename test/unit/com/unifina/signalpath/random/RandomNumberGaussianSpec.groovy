package com.unifina.signalpath.random

import com.unifina.utils.testutils.ModuleTestHelper
import groovy.transform.CompileStatic
import spock.lang.Specification

class RandomNumberGaussianSpec extends Specification {

	RandomNumberGaussian module
	
    def setup() {
		module = new RandomNumberGaussian()
		module.init()
		module.configure([options:[
		    seed: [value: 666]
		]])
    }
	
	void "RandomNumberGaussian gives the right answer"() {
		when:
		Map inputValues = [
			trigger: [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1]
		]

		Map outputValues = [
			out: generateDoubles(666, inputValues.trigger.size())
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "RandomNumberGaussian with varying params gives right answer"() {
		def doubles = generateDoubles(666, 7)

		when:
		Map inputValues = [
			mean: [-100, null, null, -0.5, 10, -1500, 0,  -30, 1000]*.doubleValue(),
			sd: [100,  null, null,  0.5,   15, -1450, 0, 0.05, 1]*.doubleValue(),
			trigger: [1, 1, 1, 1, 1, 1, 1, 1, 1]
		]

		Map outputValues = [
			out: [
				doubles[0] * 100 - 100,
				doubles[1] * 100 - 100,
				doubles[2] * 100 - 100,
				doubles[3] * 0.5 - 0.5,
				doubles[4] * 15 + 10,
				doubles[4] * 15 + 10, // nothing sent
				doubles[4] * 15 + 10, // nothing sent
				doubles[5] * 0.05 - 30,
				doubles[6] * 1 + 1000
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	/**
	 * Generate n Gaussian doubles within mean = 0, standard deviation = 1
	 */
	@CompileStatic
	static List<Double> generateDoubles(int seed, int n) {
		def r = new Random(seed)
		(1..n).collect { r.nextGaussian() }
	}
}
