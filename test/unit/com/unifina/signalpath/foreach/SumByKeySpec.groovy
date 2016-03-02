package com.unifina.signalpath.foreach

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class SumByKeySpec extends Specification {
	SumByKey module

	Map inputValues = [
		key:   ["k1", "k2", "k3", "k2", "k2", "k1", "k1", "k1", "k0", "k0", "k1"],
		value: [   1,   10,  100,   5,  2.5,     0,  0.5,    1,    0,  500,   10].collect { it.doubleValue() }
	]

	def setup() {
		module = new SumByKey()
		module.init()
		module.configure(module.getConfiguration())
	}

	void "sumByKey gives the right answer"() {
		when:
		Map outputValues = [
			"map": [
				[k1: 1],
				[k1: 1, k2: 10],
				[k1: 1, k2: 10, k3: 100],
				[k1: 1, k2: 15, k3: 100],
				[k1: 1, k2: 17.5, k3: 100],
				[k1: 1, k2: 17.5, k3: 100],
				[k1: 1.5, k2: 17.5, k3: 100],
				[k1: 2.5, k2: 17.5, k3: 100],
				[k1: 2.5, k2: 17.5, k3: 100, k0: 0],
				[k1: 2.5, k2: 17.5, k3: 100, k0: 500],
				[k1: 12.5, k2: 17.5, k3: 100, k0: 500],
			].collect { it.collectEntries { [it.key, it.value.doubleValue()] }},
			"valueOfCurrentKey": [1, 10, 100, 15, 17.5, 1, 1.5, 2.5, 0, 500, 12.5].collect { it.doubleValue() }
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "sumByKey gives the right answer (with sliding window)"() {
		module.getInput("windowLength").receive(2)

		when:
		Map outputValues = [
			"map": [
				[k1: 1],
				[k1: 1, k2: 10],
				[k1: 1, k2: 10, k3: 100],
				[k1: 1, k2: 15, k3: 100],
				[k1: 1, k2: 7.5, k3: 100],
				[k1: 1, k2: 7.5, k3: 100],
				[k1: 0.5, k2: 7.5, k3: 100],
				[k1: 1.5, k2: 7.5, k3: 100],
				[k1: 1.5, k2: 7.5, k3: 100, k0: 0],
				[k1: 1.5, k2: 7.5, k3: 100, k0: 500],
				[k1: 11, k2: 7.5, k3: 100, k0: 500],
			].collect { it.collectEntries { [it.key, it.value.doubleValue()] }},
			"valueOfCurrentKey": [1, 10, 100, 15, 7.5, 1, 0.5, 1.5, 0, 500, 11].collect { it.doubleValue() }
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "sumByKey gives the right answer (with sliding window, sorting and maxKeyCount)"() {
		module.getInput("windowLength").receive(2)
		module.getInput("sort").receive(true)
		module.getInput("maxKeyCount").receive(3)

		when:
		Map outputValues = [
			"map": [
				[k1: 1],
				[k1: 1, k2: 10],
				[k1: 1, k2: 10, k3: 100],
				[k1: 1, k2: 15, k3: 100],
				[k1: 1, k2: 7.5, k3: 100],
				[k1: 1, k2: 7.5, k3: 100],
				[k1: 0.5, k2: 7.5, k3: 100],
				[k1: 1.5, k2: 7.5, k3: 100],
				[k1: 1.5, k2: 7.5, k3: 100],
				[k2: 7.5, k3: 100, k0: 500],
				[k1: 10, k3: 100, k0: 500],
			].collect { it.collectEntries { [it.key, it.value.doubleValue()] }},
			"valueOfCurrentKey": [1, 10, 100, 15, 7.5, 1, 0.5, 1.5, 0, 500, 10].collect { it.doubleValue() }
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
