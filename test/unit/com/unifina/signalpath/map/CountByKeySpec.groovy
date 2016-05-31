package com.unifina.signalpath.map

import com.unifina.signalpath.AbstractModuleWithWindow
import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class CountByKeySpec extends Specification {
	CountByKey module

	Map inputValues = [
		key: ["k1", "k1", "k2", "k3", "k3", "k4", "k1", "k5", "k6", "k0"],
	]

	def setup() {
		module = new CountByKey()
		module.init()
	}

	void "countByKey gives the right answer"() {
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
				[k1: 2],
				[k1: 2, k2: 1],
				[k1: 2, k2: 1, k3: 1],
				[k1: 2, k2: 1, k3: 2],
				[k1: 2, k2: 1, k3: 2, k4: 1],
				[k1: 3, k2: 1, k3: 2, k4: 1],
				[k1: 3, k2: 1, k3: 2, k4: 1, k5: 1],
				[k1: 3, k2: 1, k3: 2, k4: 1, k5: 1, k6: 1],
				[k1: 3, k2: 1, k3: 2, k4: 1, k5: 1, k6: 1, k0: 1],
			].collect { it.collectEntries { [it.key, it.value.doubleValue()] } }
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "countByKey gives the right answer (sorting and maxKeyCount enabled)"() {
		module.configure([
				options: [sort: [value: true], sortOrder: [value: "descending"]],
				inputs: [
						[name: "windowLength", value: 0],
						[name: "windowType", value: AbstractModuleWithWindow.WindowType.EVENTS],
						[name: "maxKeyCount", value: 3]
				]
		])

		when:
		Map outputValues = [
			"map" : [
				[k1: 1],
				[k1: 2],
				[k1: 2, k2: 1],
				[k1: 2, k2: 1, k3: 1],
				[k1: 2, k3: 2, k2: 1],
				[k1: 2, k3: 2, k2: 1],
				[k1: 3, k3: 2, k2: 1],
				[k1: 3, k3: 2, k2: 1],
				[k1: 3, k3: 2, k2: 1],
				[k1: 3, k3: 2, k0: 1],
			].collect { it.collectEntries { [it.key, it.value.doubleValue()] } }
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "countByKey gives the right answer (sorting, ascending, and maxKeyCount enabled)"() {
		module.configure([
				options: [sort: [value: true], sortOrder: [value: "ascending"]],
				inputs: [
						[name: "windowLength", value: 0],
						[name: "windowType", value: AbstractModuleWithWindow.WindowType.EVENTS],
						[name: "maxKeyCount", value: 3]
				]
		])
		module.getInput("maxKeyCount").receive(3)

		when:
		Map outputValues = [
			"map" : [
				[k1: 1],
				[k1: 2],
				[k1: 2, k2: 1],
				[k1: 2, k2: 1, k3: 1],
				[k1: 2, k3: 2, k2: 1],
				[k1: 2, k2: 1, k4: 1],
				[k1: 3, k2: 1, k4: 1],
				[k2: 1, k4: 1, k5: 1],
				[k2: 1, k4: 1, k5: 1],
				[k2: 1, k4: 1, k0: 1],
			].collect { it.collectEntries { [it.key, it.value.doubleValue()] } }
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
