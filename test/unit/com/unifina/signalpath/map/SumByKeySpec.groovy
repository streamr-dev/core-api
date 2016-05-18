package com.unifina.signalpath.map

import com.unifina.signalpath.AbstractModuleWithWindow
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
		module.configure([
				options: [sort: [value: false]],
				inputs: [
						[name: "windowLength", value: 0],
						[name: "windowType", value: AbstractModuleWithWindow.WindowType.EVENTS],
						[name: "maxKeyCount", value: 0]
				]
		])

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
			].collect { it.collectEntries { [it.key, it.value.doubleValue()] }}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "sumByKey gives the right answer (with sliding window)"() {
		module.configure([
				options: [sort: [value: false]],
				inputs: [
						[name: "windowLength", value: 2],
						[name: "windowType", value: AbstractModuleWithWindow.WindowType.EVENTS],
						[name: "maxKeyCount", value: 0]
				]
		])

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
			].collect { it.collectEntries { [it.key, it.value.doubleValue()] }}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "sumByKey gives the right answer (with sliding window, sorting and maxKeyCount)"() {
		module.configure([
				options: [sort: [value: true], sortOrder: [value: "descending"], deleteEmptyKeys: [value: false]],
				inputs: [
						[name: "windowLength", value: 2],
						[name: "windowType", value: AbstractModuleWithWindow.WindowType.EVENTS],
						[name: "maxKeyCount", value: 3]
				]
		])

		when:
		Map outputValues = [
			"map": [								// Values added:
				[k1: 1],							// k1: 1
				[k1: 1, k2: 10],					// k2: 10
				[k1: 1, k2: 10, k3: 100],			// k3: 100
				[k1: 1, k2: 15, k3: 100],			// k2: 5
				[k1: 1, k2: 7.5, k3: 100],			// k2: 2.5
				[k1: 1, k2: 7.5, k3: 100],			// k1: 0
				[k1: 0.5, k2: 7.5, k3: 100],		// k1: 0.5
				[k1: 1.5, k2: 7.5, k3: 100],		// k1: 1
				[k1: 1.5, k2: 7.5, k3: 100],		// k0: 0 (k0 pruned)
				[k2: 7.5, k3: 100, k0: 500],		// k0: 500	(k1 pruned)
				[k1: 10, k3: 100, k0: 500],			// k1: 10
			].collect { it.collectEntries { [it.key, it.value.doubleValue()] }}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "sumByKey gives the right answer (with sliding time window and deleteEmptyKeys)"() {
		module.configure([
				options: [sort: [value: false], deleteEmptyKeys: [value: true]],
				inputs: [
						[name: "windowLength", value: 5],
						[name: "windowType", value: AbstractModuleWithWindow.WindowType.SECONDS],
						[name: "maxKeyCount", value: 0]
				]
		])

		Map ticks = [:]
		(0..10).each {
			ticks.put(it, new Date(it * 1000))
		}

		when:
		Map outputValues = [
				"map": [								// Values added:
						[k1: 1],						// k1: 1
						[k1: 1, k2: 10],				// k2: 10
						[k1: 1, k2: 10, k3: 100],		// k3: 100
						[k1: 1, k2: 15, k3: 100],		// k2: 5
						[k1: 1, k2: 17.5, k3: 100],		// k2: 2.5					Values falling off the window:
						[k1: 0, k2: 17.5, k3: 100], 	// k1: 0					k1: 1
						[k1: 0.5, k2: 7.5, k3: 100], 	// k1: 0.5					k2: 10
						[k1: 1.5, k2: 7.5], 			// k1: 1					k3: 100, k3 empty
						[k1: 1.5, k2: 2.5, k0:0], 		// k0: 0					k2: 5
						[k1: 1.5, k0:500], 				// k0: 500					k2: 2.5, k2 empty
						[k1: 11.5, k0: 500], 			// k1: 10					k1: 0
				].collect { it.collectEntries { [it.key, it.value.doubleValue()] }}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
				.ticks(ticks)
				.test()
	}

}
