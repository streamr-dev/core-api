package com.unifina.signalpath.random

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class RandomStringSpec extends Specification {

	RandomString module
	Random testRandom = new Random(666)

	def setup() {
		module = new RandomString()
		module.init()
		module.configure([options:[
			seed: [value: 666]
		]])
	}

	void "RandomString gives the right answer"() {
		char[] symbols = module.configuration.get("options").get("symbols").getString().toCharArray()

		when:
		Map inputValues = [
			length: [10, null, null, null, null, 2, 4, 1, 0, 64],
			trigger: (1..10).collect { 1 }
		]

		Map outputValues = [
			out: [
				RandomString.sampleString(symbols, 10, testRandom),
				RandomString.sampleString(symbols, 10, testRandom),
				RandomString.sampleString(symbols, 10, testRandom),
				RandomString.sampleString(symbols, 10, testRandom),
				RandomString.sampleString(symbols, 10, testRandom),
				RandomString.sampleString(symbols, 2, testRandom),
				RandomString.sampleString(symbols, 4, testRandom),
				RandomString.sampleString(symbols, 1, testRandom),
				RandomString.sampleString(symbols, 0, testRandom),
				RandomString.sampleString(symbols, 64, testRandom),
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
