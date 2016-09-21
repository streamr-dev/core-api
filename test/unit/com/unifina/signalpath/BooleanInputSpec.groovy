package com.unifina.signalpath

import spock.lang.Specification

class BooleanInputSpec extends Specification {
	BooleanInput input = new BooleanInput(Mock(AbstractSignalPathModule), "input")


	def setup() {
		input.clear()
		input.drivingInput = false
	}

	def "booleanInput works with Booleans"() {
		when: "inputted true"
			input.receive(true)
		then: "value is true"
			input.getValue()
		when: "inputted false"
			input.receive(false)
		then: "value is false"
			!input.getValue()
	}

	def "booleanInput works with Doubles"() {
		when: "inputted positive value"
			input.receive(1d)
		then: "value is true"
			input.getValue()
		when: "inputted 0"
			input.receive(0d)
		then: "value is false"
			!input.getValue()
		when: "inputted negative value"
			input.receive(-1d)
		then: "value is true"
			input.getValue()
	}

	def "booleanInput works with Integers"() {
		when: "inputted positive value"
			input.receive(1)
		then: "value is true"
			input.getValue()
		when: "inputted 0"
			input.receive(0)
		then: "value is false"
			!input.getValue()
		when: "inputted negative value"
			input.receive(-1)
		then: "value is true"
			input.getValue()
	}

	def "booleanInput works with Strings"() {
		when: "inputted empty string"
			input.receive("")
		then: "value is false"
			!input.getValue()
		when: "inputted not-empty string"
			input.receive("test")
		then: "value is true"
			input.getValue()
		when: "inputted 'false'"
			input.receive("false")
		then: "value is false"
			!input.getValue()
	}

	def "booleanInput works with Lists"() {
		when: "inputted empty list"
			input.receive([])
		then: "value is false"
			!input.getValue()
		when: "inputted not-empty list"
			input.receive([1, 2])
		then: "value is true"
			input.getValue()
	}

	def "booleanInput works with Maps"() {
		when: "inputted empty map"
			input.receive([:])
		then: "value is false"
			!input.getValue()
		when: "inputted not-empty map"
			input.receive([a:1])
		then: "value is true"
			input.getValue()
	}

	def "booleanInput works with other types"() {
		when: "inputted null"
			input.receive(null)
		then: "value is false"
			!input.getValue()
		when: "inputted not-null object"
			input.receive(new BooleanOutput(Mock(AbstractSignalPathModule), "input"))
		then: "value is true"
			input.getValue()
	}
}
