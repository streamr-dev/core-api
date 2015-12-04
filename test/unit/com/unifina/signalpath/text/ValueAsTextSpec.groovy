package com.unifina.signalpath.text

import spock.lang.Specification

import com.unifina.utils.testutils.ModuleTestHelper

class ValueAsTextSpec extends Specification {
	
	def module

	def setup(){
		module = null
	}

    def cleanup() {
		
    }
	
	void "ValueAsText works properly"() {
		module = new ValueAsText()
		module.init()
		when:
		int integer = 1234
		ArrayList<Integer> list = new ArrayList<Integer>()
		list.add(1)
		list.add(2)
		list.add(3)
		HashMap<Integer, Map<String, ArrayList<String>>> map = new HashMap<Integer, Map<String, ArrayList<String>>>()
		map.put(0, new HashMap<String, ArrayList<String>>())
		map.get(0).put("a", new ArrayList<String>())
		map.get(0).get("a").add("a")
		map.get(0).get("a").add("b")

		Map inputValues = [
			in: [integer, list, map, "string"]
		]
		Map outputValues = [
			text: ["1234", "[1, 2, 3]", "{0={a=[a, b]}}" , "string"]
		]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
