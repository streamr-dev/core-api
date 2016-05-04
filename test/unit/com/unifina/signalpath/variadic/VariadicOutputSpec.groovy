package com.unifina.signalpath.variadic

import com.unifina.signalpath.AbstractSignalPathModule
import spock.lang.Specification

class VariadicOutputSpec extends Specification {
	def module = Mock(AbstractSignalPathModule)
	def variadicOutput = new VariadicOutput<Object>(module, new OutputInstantiator.SimpleObject())

	def "outputs are created"() {
		when:
		variadicOutput.onConfiguration([
			outputNames: ["endpoint-a", "endpoint-b", "endpoint-c"]
		])
		then:
		variadicOutput.endpoints.size() == 3
	}

	def "created outputs' display names are as expected"() {
		when:
		variadicOutput.onConfiguration([
			outputNames: ["endpoint-a", "endpoint-b", "endpoint-c"]
		])
		then:
		variadicOutput.endpoints*.displayName == ["out1", "out2", "out3"]
	}

	def "created outputs + placeholder are registered with module"() {
		when:
		variadicOutput.onConfiguration([
			outputNames: ["endpoint-a", "endpoint-b", "endpoint-c"]
		])
		then:
		4 * module.addOutput(_)
		0 * module._
	}

	def "created outputs can be sent to"() {
		variadicOutput.onConfiguration([
			outputNames: ["endpoint-a", "endpoint-b", "endpoint-c"]
		])

		when:
		variadicOutput.send(["hello", "world", 512])
		then:
		variadicOutput.endpoints*.getValue() == ["hello", "world", 512]
	}

	def "nulls are ignored when sending to outputs"() {
		variadicOutput.onConfiguration([
			outputNames: ["endpoint-a", "endpoint-b", "endpoint-c"]
		])

		when:
		variadicOutput.send(["hello", null, 512])
		then:
		notThrown(NullPointerException)
		variadicOutput.endpoints*.getValue() == ["hello", null, 512]
	}

	def "attempting to send mismatching list size to outputs causes IllegalArgumentException"() {
		variadicOutput.onConfiguration([:])

		when:
		variadicOutput.send(["hello", 512])
		then:
		thrown(IllegalArgumentException)
	}

	def "getConfiguration() provides output count and names, and module configuration"() {
		variadicOutput.onConfiguration([
			outputNames: ["endpoint-a", "endpoint-b", "endpoint-c"]
		])

		when:
		def config = [:]
		variadicOutput.getConfiguration(config)
		then:
		config.keySet() == ["outputNames", "variadicOutput"] as Set
		config.outputNames.size() == 3
		config.variadicOutput == true
	}

	def "placeholder output"() {
		when:
		variadicOutput.onConfiguration([
			outputNames: ["endpoint-a", "endpoint-b", "endpoint-c"]
		])

		then: "display name is as expected"
		variadicOutput.placeholder.displayName == "out4"
	}
}
