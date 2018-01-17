package com.unifina.signalpath.random

import com.unifina.utils.testutils.ModuleTestHelper
import groovy.transform.CompileStatic
import spock.lang.Specification

class RandomNumberSpec extends Specification {
	
	RandomNumber module
	
    def setup() {
		module = new RandomNumber()
		module.init()
		module.configure([options:[
		    seed: [value: 666]
		]])
    }
	
	void "RandomNumber gives the right answer"() {
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

	void "RandomNumber with varying params gives right answer"() {
		def doubles = generateDoubles(666, 7)

		when:
		Map inputValues = [
			min: [-100, null, null, -0.5, 10, -1500, 0,  -15, 1000]*.doubleValue(),
			max: [100,  null, null,  0.5,   15, -1450, 0, -5, -555]*.doubleValue(),
			trigger: [1, 1, 1, 1, 1, 1, 1, 1, 1]
		]

		Map outputValues = [
			out: [
				doubles[0] * 100,
				doubles[1] * 100,
				doubles[2] * 100,
				doubles[3] * 0.5,
				doubles[4] * 2.5 + 12.5,
				doubles[5] * 25 - 1475.0,
				doubles[5] * 25 - 1475.0, // nothing sent
				doubles[6] * 5 - 10,
				doubles[6] * 5 - 10, // nothing sent
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	/**
	 * Generate n uniform doubles within [-1,1]
	 */
	@CompileStatic
	static List<Double> generateDoubles(int seed, int n) {
		def r = new Random(seed)
		(1..n).collect { (r.nextDouble() - 0.5) * 2 }
	}
}
