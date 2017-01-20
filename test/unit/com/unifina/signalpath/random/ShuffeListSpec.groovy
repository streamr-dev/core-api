package com.unifina.signalpath.random

import com.unifina.utils.testutils.ModuleTestHelper
import groovy.transform.CompileStatic
import spock.lang.Specification

class ShuffeListSpec extends Specification {

	ShuffleList module
	
    def setup() {
		module = new ShuffleList()
		module.init()
		module.configure([options:[
		    seed: [value: 666]
		]])
    }
	
	void "ShuffleList() gives the right answer"() {
		when:
		Map inputValues = [
			in: [[], (1..100).toList(), ["a", "b", "c", "d", "e"]]
		]

		Map outputValues = [
			out: shuffleLists(666, [[], (1..100).toList(), ["a", "b", "c", "d", "e"]])
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	@CompileStatic
	static List<List<?>> shuffleLists(int seed, List<List<?>> list) {
		def r = new Random(seed)
		list.each { List l -> Collections.shuffle(l, r) }
		return list
	}
}
