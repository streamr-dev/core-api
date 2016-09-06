package com.unifina.signalpath.variadic

import com.unifina.signalpath.AbstractSignalPathModule
import spock.lang.Specification

class VariadicOutputSpec extends Specification {
	def module = Mock(AbstractSignalPathModule)
	def variadicOutput = new VariadicOutput<Object>(module, new OutputInstantiator.SimpleObject())

	def "attachToModule() attaches endpoint (Output) to module"() {
		when:
		variadicOutput.addEndpoint("new-output-1")
		variadicOutput.addEndpoint("new-output-2")

		then:
		2 * module.addOutput(_)
	}

	def "created outputs have expected displayName and jsClass"() {
		when:
		variadicOutput.addEndpoint("new-output-1")
		variadicOutput.addEndpoint("new-output-2")

		then:
		variadicOutput.endpointsIncludingPlaceholder*.displayName == ["out1", "out2"]
		variadicOutput.endpointsIncludingPlaceholder*.getConfiguration()*.jsClass == ["VariadicOutput", "VariadicOutput"]
	}

	def "can send data to (non-placeholder) outputs"() {
		variadicOutput.addEndpoint("new-output-1")
		variadicOutput.addEndpoint("new-output-2")
		variadicOutput.addEndpoint("new-output-3")

		when:
		variadicOutput.send([666, "hello"])

		then:
		variadicOutput.endpoints*.value == [666, "hello"]
	}

	def "any nulls in list to be sent to (non-placeholder) outputs are ignored"() {
		variadicOutput.addEndpoint("new-output-1")
		variadicOutput.addEndpoint("new-output-2")
		variadicOutput.addEndpoint("new-output-3")

		when:
		variadicOutput.send([null, "hello"])

		then:
		variadicOutput.endpoints*.value == [null, "hello"]
	}

	def "cannot send data of unmatching size"() {
		variadicOutput.addEndpoint("new-output-1")
		variadicOutput.addEndpoint("new-output-2")
		variadicOutput.addEndpoint("new-output-3")

		when:
		variadicOutput.send([666, "hello", "world"])

		then:
		thrown(IllegalArgumentException)
	}
}
