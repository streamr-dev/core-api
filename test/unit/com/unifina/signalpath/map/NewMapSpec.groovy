package com.unifina.signalpath.map

import com.unifina.utils.Globals
import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class NewMapSpec extends Specification {
	NewMap module

	def setup() {
		module = new NewMap()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "newMapSpec works as expected"() {
		Map inputValues = [
			trigger: ["hello", new Object(), "world!",   666,  999]
		]

		Map outputValues = [
			out: [[:], [:], [:], [:], [:]]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	def "newMapSpec by default returns same map reference"() {
		module.globals = new Globals()
		module.connectionsReady()

		def outputMaps = (1..10).collect {
			module.getInput("trigger").receive(new Object())
			module.sendOutput()
			module.getOutput("out").getValue()
		}

		expect:
		outputMaps.every { it.equals(outputMaps.first()) }
	}

	def "newMapSpec can be configured to return different map reference"() {
		module.globals = new Globals()
		module.connectionsReady()
		module.getInput("alwaysNew").receive(true)

		def outputMaps = (1..3).collect {
			module.getInput("trigger").receive(new Object())
			module.sendOutput()
			module.getOutput("out").getValue()
		}

		expect:
		!outputMaps[0].is(outputMaps[1])
		!outputMaps[0].is(outputMaps[2])
		!outputMaps[0].is(outputMaps[3])
		!outputMaps[1].is(outputMaps[2])
		!outputMaps[1].is(outputMaps[3])
		!outputMaps[2].is(outputMaps[3])
	}
}
