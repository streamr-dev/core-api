package com.unifina.signalpath.variadic

import com.unifina.signalpath.AbstractSignalPathModule
import spock.lang.Specification

class VariadicInputSpec extends Specification {
	def module = Mock(AbstractSignalPathModule)
	def variadicInput = new VariadicInput<Object>(module, 3, new InputInstantiator.SimpleObject())

	def "inputs are created"() {
		when:
		variadicInput.onConfiguration([:])
		then:
		variadicInput.endpoints.size() == 3
	}

	def "created inputs' display names are as expected"() {
		when:
		variadicInput.onConfiguration([:])
		then:
		variadicInput.endpoints*.displayName == ["in1", "in2", "in3"]
	}

	def "created inputs are registered with module"() {
		when:
		variadicInput.onConfiguration([:])

		then:
		3 * module.addInput(_)
		0 * module._
	}

	def "getValues() collects input values"() {
		module.drivingInputs = [] // prevent null pointer exception

		variadicInput.onConfiguration([:])
		variadicInput.endpoints.get(0).receive(Double.valueOf(666))
		variadicInput.endpoints.get(1).receive("hello world")

		expect:
		variadicInput.values == [666D, "hello world", null]
	}

	def "getConfiguration() provides input count and module configuration"() {
		variadicInput.onConfiguration([:])

		when:
		def config = [:]
		variadicInput.getConfiguration(config)

		then:
		config == [
			variadicInput: true,
			options: [
				inputs: [value: 3, type: "int"]
			]
		]
	}
}
