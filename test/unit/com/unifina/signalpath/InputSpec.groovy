package com.unifina.signalpath

import com.unifina.signalpath.simplemath.Multiply
import spock.lang.Specification

class InputSpec extends Specification {
	def owner = Stub(AbstractSignalPathModule) {
		getDrivingInputs(_) >> new HashSet<Input>()
	}
	def input = new Input<Integer>(owner, "input", "Integer");

	def setup() {
		owner.getName() >> "owner"
		owner.drivingInputs = new HashSet<>()
	}

	def "on instantiation its value is null"() {
		expect:
		!input.hasValue()
	}

	def "on instantiation ready and wasReady false"() {
		expect:
		!input.ready
		!input.wasReady
	}

	def "on instantiation is in driving mode"() {
		expect:
		input.isDrivingInput()
	}

	def "toString gives textual representation of object"() {
		input.receive(64)
		expect:
		input.toString() == "(in) owner.input, value: 64"
	}

	def "calling receive changes value, ready flags, and invokes markReady() on owner"() {
		def owner = Mock(AbstractSignalPathModule)
		input.setOwner(owner)

		when:
		input.receive(64)

		then:
		input.value == 64
		input.ready
		input.wasReady
		1 * owner.getDrivingInputs() >> new HashSet<>()
		1 * owner.markReady(input)
	}

	def "calling receive sets owner to pending by default (driving mode)"() {
		def owner = Mock(AbstractSignalPathModule)
		input.setOwner(owner)

		when:
		input.receive(64)

		then:
		1 * owner.getDrivingInputs() >> new HashSet<>([input])
		1 * owner.setSendPending(true)
	}

	def "calling receive does not set owner to pending if input flagged non-driving"() {
		def owner = Mock(AbstractSignalPathModule)
		input.setOwner(owner)
		input.drivingInput = false

		when:
		input.receive(64)

		then:
		0 * owner.setSendPending(_)
	}

	def "calling receive delegates value to other inputs when proxying"() {
		def input1 = new Input<Integer>(new Multiply(), "input1", "Integer")
		def input2 = new Input<Integer>(new Multiply(), "input2", "Integer")

		input.addProxiedInput(input1)
		input.addProxiedInput(input2)

		when:
		input.receive(64)

		then:
		input1.value == 64
		input2.value == 64
	}

	def "if proxied inputs are added later on, the last receive value (if exists) is delegated"() {
		input.receive(64)

		def input1 = new Input<Integer>(new Multiply(), "input1", "Integer")
		def input2 = new Input<Integer>(new Multiply(), "input2", "Integer")

		when:
		input.addProxiedInput(input1)
		input.addProxiedInput(input2)

		then:
		input1.value == 64
		input2.value == 64
	}
}
