package com.unifina.signalpath

import spock.lang.Specification

class ModuleOptionSpec extends Specification {

	void "getKey() retrieves key"() {
		expect:
		ModuleOption.createInt("KeY", 0).getKey() == "KeY"
	}

	void "getValue() retrieves value"() {
		expect:
		ModuleOption.createInt("KeY", 0).getValue() == 0
		ModuleOption.createDouble("KeY", 3.141).getValue() == 3.141
	}

	void "createBoolean creates correct map structure"() {
		expect:
		ModuleOption.createBoolean("boolKey", true) as Map == [value: true, type: "boolean"]
	}

	void "createInt creates correct map structure"() {
		expect:
		ModuleOption.createInt("intKey", 512) as Map == [value: 512, type: "int"]
	}

	void "createDouble creates correct map structure"() {
		expect:
		ModuleOption.createDouble("doubleKey", 512.256) as Map == [value: 512.256, type: "double"]
	}

	void "createString creates correct map structure"() {
		expect:
		ModuleOption.createString("stringKey", "hello world") as Map == [value: "hello world", type: "string"]
	}

	void "getBoolean returns correct value"() {
		expect:
		ModuleOption.createBoolean("boolKey", true).getBoolean() == Boolean.TRUE
		ModuleOption.createString("stringKey", "false").getBoolean() == Boolean.FALSE
	}

	void "getInt returns correct value"() {
		expect:
		ModuleOption.createInt("intKey", 512).getInt() == 512
		ModuleOption.createString("stringKey", "512").getInt() == 512
	}

	void "getDouble returns correct value"() {
		expect:
		ModuleOption.createDouble("doubleKey", 512.256).getDouble() == 512.256d
		ModuleOption.createString("stringKey", "512.256").getDouble() == 512.256d
	}

	void "getString returns correct value"() {
		expect:
		ModuleOption.createString("stringKey", "hello world").getString() == "hello world"
		ModuleOption.createBoolean("boolKey", true).getString() == "true"
		ModuleOption.createInt("intKey", 512).getString() == "512"
		ModuleOption.createDouble("doubleKey", 512.256).getString() == "512.256"
	}

	void "addPossibleValues creates correct"() {
		when:
		ModuleOption moduleOption = ModuleOption.createString("key", "value0")
			.addPossibleValue("Value 1", "value1")
			.addPossibleValue("Value 2", "value2")
			.addPossibleValue("Value 3", "value3")
		then:
		moduleOption as Map == [
			value: "value0",
			type: "string",
			possibleValues: [
			    [text: "Value 1", value: "value1"],
				[text: "Value 2", value: "value2"],
				[text: "Value 3", value: "value3"]
			]
		]
	}
}
