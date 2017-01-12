package com.unifina.signalpath

import com.unifina.signalpath.text.ConstantString
import spock.lang.Specification

class EnumParameterSpec extends Specification {

	static enum TestEnum {
		A, B, C

		@Override
		String toString() {
			return "toString-" + name()
		}
	}

	EnumParameter<TestEnum> parameter = new EnumParameter<>(Stub(AbstractSignalPathModule), "name", TestEnum.values())

	def "constructor fails if given no enum values"() {
		when:
		TestEnum[] values = []
		new EnumParameter(null, "name", values)

		then:
		thrown(ArrayIndexOutOfBoundsException)
	}

	def "getPossibleValues() lists enums with results of toString() as names and name() as values"() {
		expect:
		parameter.possibleValues == [
			new PossibleValue("toString-A", "A"),
			new PossibleValue("toString-B", "B"),
			new PossibleValue("toString-C", "C"),
		]
	}

	def "parseValue() returns enum value for string"() {
		expect:
		parameter.parseValue("B") == TestEnum.B
	}

	def "parseValue() throws exception if given invalid string"() {
		when:
		parameter.parseValue("NON-EXISTING")

		then:
		thrown(IllegalArgumentException)
	}

	def "formatValue() shows result of name()"() {
		expect:
		parameter.formatValue(TestEnum.A) == "A"
	}

	def "getTypeClass() works"() {
		expect:
		parameter.typeClass == TestEnum
	}

	def "getValue() works with default value"() {
		expect:
		parameter.getValue() == TestEnum.A
	}

	def "getValue() works with received enum value"() {
		parameter.receive(TestEnum.B)

		expect:
		parameter.getValue() == TestEnum.B
	}

	def "getValue() works with received string value"() {
		parameter.receive("B")

		expect:
		parameter.getValue() == TestEnum.B
	}

	def "getValue() works when connected to string output"() {
		def constantStringModule = new ConstantString()
		constantStringModule.init()
		Parameter strParam = constantStringModule.getInput("str")
		strParam.setDefaultValue("C")
		constantStringModule.getOutput("out").connect(parameter)

		expect:
		parameter.getValue() == TestEnum.C
	}
}
