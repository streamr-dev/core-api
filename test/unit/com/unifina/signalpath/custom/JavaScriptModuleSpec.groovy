package com.unifina.signalpath.custom

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class JavaScriptModuleSpec extends Specification {
	def module = new JavaScriptModule()

	def setup() {
		module.init()
	}

	void "JavaScriptModule works as expected"() {
		when:
		module.configure([
			uiChannel: [id: "uiChannel"],
			code: """
				var characters = [];
				var times = {};
				var a = 0;
				
				function addCharacter(character) {
					characters.push(character);
					if (times[character] === undefined) {
						system.debug("1st occurrence of " + character);
						times[character] = [];
					}
					times[character].push(system.time());
				}
				
				function getA() {
					return a;
				}
				
				function getCharacters() {
					return characters.join(",");
				}
				
				function getTimes() {
					return times;
				}
			"""
		])

		Map inputValues = [
			function: [
				"addCharacter",
				null,
				null,
				null,
				null,
				"getCharacters",
				"getA",
				"addCharacter",
				null,
				null,
				null,
				"getCharacters",
				"getTimes",
			],
			arguments: [
				["a"],
				["b"],
				["c"],
				["d"],
				["a"],
				[],
				[],
				["a"],
				["c"],
				["z"],
				["z"],
				[],
				[],
			],
			"characters (list)": [
				[],
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				["x", "y"],
				null,
				null,
				null,
			],
		]
		Map outputValues = [
		    return: [
				null,
				null,
				null,
				null,
				null,
				"a,b,c,d,a",
				0d,
				0d,
				0d,
				0d,
				0d,
				"x,y,z,z",
				[a: [0d, 4d, 7d], b: [1d], c:[2d, 8d], d: [3d], z: [9d, 10d]]
			]
		]
		Map uiChannel = [:]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(uiChannel)
			.timeToFurtherPerIteration(1)
			.test()
	}
}
