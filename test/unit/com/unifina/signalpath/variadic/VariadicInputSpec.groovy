package com.unifina.signalpath.variadic

import com.unifina.signalpath.AbstractSignalPathModule
import spock.lang.Specification

class VariadicInputSpec extends Specification {
	def module = Mock(AbstractSignalPathModule)
	def variadicInput = new VariadicInput<Object>(module, new InputInstantiator.SimpleObject())

	def "inputs are created"() {
		when:
		variadicInput.onConfiguration([
			inputNames: ["endpoint-a", "endpoint-b", "endpoint-c"]
		])
		then:
		variadicInput.endpoints.size() == 3
	}

	def "created inputs' display names are as expected"() {
		when:
		variadicInput.onConfiguration([
			inputNames: ["endpoint-a", "endpoint-b", "endpoint-c"]
		])
		then:
		variadicInput.endpoints*.displayName == ["in1", "in2", "in3"]
	}

	def "created inputs + placeholder are registered with module"() {
		when:
		variadicInput.onConfiguration([
			inputNames: ["endpoint-a", "endpoint-b", "endpoint-c"]
		])
		then:
		4 * module.addInput(_)
		0 * module._
	}

	def "getValues() collects input values"() {
		module.drivingInputs = [] // prevent null pointer exception

		variadicInput.onConfiguration([
			inputNames: ["endpoint-a", "endpoint-b", "endpoint-c"]
		])
		variadicInput.endpoints.get(0).receive(Double.valueOf(666))
		variadicInput.endpoints.get(1).receive("hello world")

		expect:
		variadicInput.values == [666D, "hello world", null]
	}

	def "getConfiguration() provides input count and names, and module configuration"() {
		variadicInput.onConfiguration([
			inputNames: ["endpoint-a", "endpoint-b", "endpoint-c"]
		])

		when:
		def config = [:]
		variadicInput.getConfiguration(config)

		then:
		config.keySet() == ["inputNames", "variadicInput"] as Set
		config.inputNames.size() == 3
		config.variadicInput == true
	}

	def "placeholder input"() {
		when:
		variadicInput.onConfiguration([
			inputNames: ["endpoint-a", "endpoint-b", "endpoint-c"]
		])

		then: "requiresConnection=false"
		!variadicInput.placeholder.requiresConnection
		and: "display name is as expected"
		variadicInput.placeholder.displayName == "in4"
	}
}
