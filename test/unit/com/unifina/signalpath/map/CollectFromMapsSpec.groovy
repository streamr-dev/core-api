package com.unifina.signalpath.map

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class CollectFromMapsSpec extends Specification {
	CollectFromMaps module

	def setup() {
		module = new CollectFromMaps()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "CollectFromMaps works as expected"() {

		def mapOfMapsExample = [
			mapOne: [
				"a": 5,
				"b": "hello",
				"c": [d: true]
			],
			mapTwo: [
				"a": 13,
				"b": "world",
				"c": [d: false, e: "e"]
			],
			mapThree: [
				"a": 666,
				"b": "!",
				"c": "mine"
			],
		]

		def listOfMapsExample = [
			[
				"a": 5,
				"b": "hello",
				"c": [d: true]
			],
			[
				"a": 13,
				"b": "world",
				"c": [d: false, e: "e"]
			],
			[
				"a": 666,
				"b": "!",
				"c": "mine"
			],
		]

		when:
		Map inputValues = [
			selector: [
				"",
				"b",
				"c",
				"c.d",
				"non.existent.path.this.be",
				"a",
				"c",
				"c.d"
			],
			listOrMap: [
				mapOfMapsExample,
				mapOfMapsExample,
				mapOfMapsExample,
				mapOfMapsExample,
				[:],
				listOfMapsExample,
				listOfMapsExample,
				listOfMapsExample,
			]
		]

		Map outputValues = [
			listOrMap: [
				mapOfMapsExample,
				[mapOne: "hello", mapTwo: "world", mapThree: "!"],
				[mapOne: [d: true], mapTwo: [d: false, e: "e"], mapThree: "mine"],
				[mapOne: true, mapTwo: false],
				[:],
				[5, 13, 666],
				[[d: true], [d: false, e: "e"], "mine"],
				[true, false]
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
