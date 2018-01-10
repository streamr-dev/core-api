package com.unifina.signalpath.variadic

import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.Input
import spock.lang.Specification

class VariadicInputSpec extends Specification {
	def module = Stub(AbstractSignalPathModule) {
		module.getDrivingInputs(_) >> new HashSet<Input>()
	}
	def variadicInput = new VariadicInput<Object>(module, new InputInstantiator.SimpleObject())

	def "attachToModule() attaches endpoint (Input) to module"() {
		module = Mock(AbstractSignalPathModule)
		variadicInput = new VariadicInput<Object>(module, new InputInstantiator.SimpleObject())

		when:
		variadicInput.addEndpoint("new-input-1")
		variadicInput.addEndpoint("new-input-2")

		then:
		2 * module.addInput(_)
	}

	def "created inputs have expected displayName and jsClass"() {
		when:
		variadicInput.addEndpoint("new-input-1")
		variadicInput.addEndpoint("new-input-2")

		then:
		variadicInput.endpointsIncludingPlaceholder*.displayName == ["in1", "in2"]
		variadicInput.endpointsIncludingPlaceholder*.getConfiguration()*.jsClass == ["VariadicInput", "VariadicInput"]
	}

	def "can getValues() of non-placeholder input"() {
		variadicInput.addEndpoint("new-input-1")
		variadicInput.addEndpoint("new-input-2")
		variadicInput.addEndpoint("new-input-3")
		variadicInput.onConfiguration([:])

		variadicInput.endpoints[0].receive(666)
		variadicInput.endpoints[1].receive("hello world")

		when:
		List values = variadicInput.getValues()

		then:
		values == [666, "hello world"]
	}
}
