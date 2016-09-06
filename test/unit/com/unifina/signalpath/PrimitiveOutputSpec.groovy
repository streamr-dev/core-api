package com.unifina.signalpath

import spock.lang.Specification

class PrimitiveOutputSpec extends Specification {
	def owner = Mock(AbstractSignalPathModule)
	def output = new PrimitiveOutput<Integer>(owner, "output", "Integer");
	def input = Mock(Input)

	def setup() {
		output.connect(input)
	}

	def "with noRepeat = true, invoking send() with same value does not send an event"() {
		output.noRepeat = true

		when:
		output.send(512)
		output.send(512)

		then:
		output.getValue() == 512
		1 * input.receive(512)
	}

	def "with noRepeat = true, invoking send() with different values works normally"() {
		output.noRepeat = true

		when:
		output.send(512)
		output.send(511)

		then:
		output.getValue() == 511
		1 * input.receive(512)
		1 * input.receive(511)
	}

	def "with noRepeat = false, invoking send() with same value sends all events"() {
		output.noRepeat = false

		when:
		output.send(512)
		output.send(512)

		then:
		2 * input.receive(512)
	}

	def "noRepeat value is written into configuration"() {
		when:
		output.noRepeat = true
		then:
		output.getConfiguration().noRepeat

		when:
		output.noRepeat = false
		then:
		!output.getConfiguration().noRepeat
	}

	def "noRepeat value is read from configuration"() {
		when:
		output.setConfiguration([noRepeat: true])
		then:
		output.noRepeat

		when:
		output.setConfiguration([noRepeat: false])
		then:
		!output.noRepeat
	}
}
