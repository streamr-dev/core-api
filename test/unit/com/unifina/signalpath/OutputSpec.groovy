package com.unifina.signalpath

import spock.lang.Specification

class OutputSpec extends Specification {
	def owner = Stub(AbstractSignalPathModule) {
		getDrivingInputs(_) >> new HashSet<Input>()
	}
	def output = new Output<Integer>(owner, "output", "Integer");
	def in1 = new Input<Integer>(owner, "in1", "Integer")
	def in2 = new Input<Integer>(owner, "in2", "Integer")

	def setup() {
		in1.owner.drivingInputs = new HashSet<>()
		in2.owner.drivingInputs = new HashSet<>()
	}

	def "not connected and null value just after instantiation"() {
		expect:
		!output.connected
		output.value == null
		output.targets == []
	}

	def "connecting inputs changes state to connected"() {
		when:
		output.connect(in1)
		output.connect(in2)

		then:
		output.connected
		output.targets == [in1, in2]
		in1.source == output
		in2.source == output
	}

	def "invoking send() with null causes exception"() {
		setup:
		output.connect(in1)
		output.connect(in2)

		when:
		output.send(null)

		then:
		thrown(NullPointerException)
	}

	def "invoking send() changes value and passes it to inputs"() {
		setup:
		output.connect(in1)
		output.connect(in2)

		when:
		output.send(512)

		then:
		output.getValue() == 512
		in1.getValue() == 512
		in2.getValue() == 512
	}

	def "invoking send() sets propagators pending flags on"() {
		setup:
		output.connect(in1)
		output.connect(in2)

		def p = new Propagator()
		assert !p.sendPending
		output.addPropagator(p)

		when:
		output.send(512)

		then:
		p.sendPending
	}

	def "invoking send() delegates to any proxies"() {
		setup:
		def proxyOutput1 = new Output<Integer>(owner, "proxyOutput", "Integer");
		def proxyOutput2 = new Output<Integer>(owner, "proxyOutput", "Integer");
		output.addProxiedOutput(proxyOutput1)
		output.addProxiedOutput(proxyOutput2)

		when:
		output.send(1024)

		then:
		proxyOutput1.getValue() == 1024
		proxyOutput2.getValue() == 1024
	}
}
