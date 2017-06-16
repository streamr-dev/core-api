package com.unifina.signalpath

import spock.lang.Specification

class ModuleOptionsSpec extends Specification {

	Map<String, Object> parentConfig = [:]

	void "get() sets up empty options object if existing not found in parent config"() {
		when:
		def options = ModuleOptions.get(parentConfig)

		then:
		options instanceof ModuleOptions
		options as Map == [:]
		parentConfig == [options: options]
	}

	void "get() reads options if existing in parentConfig as Map"() {
		parentConfig["options"] = [
			a: [value: true, type: "boolean"],
			b: [value: "b", type: "string"],
			c: [value: 0, type: "int"]
		]

		when:
		def options = ModuleOptions.get(parentConfig)

		then:
		options instanceof ModuleOptions
		options as Map == [
			a: [value: true, type: "boolean"],
			b: [value: "b", type: "string"],
			c: [value: 0, type: "int"]
		]
		parentConfig["options"].is(options)
	}

	void "get() reads options if existing in parentConfig as ModuleOptions"() {
		def originalOptions = new ModuleOptions()
		originalOptions["a"] = [value: true, type: "boolean"]
		originalOptions["b"] = [value: "b", type: "string"]
		originalOptions["c"] = [value: 0, type: "int"]

		parentConfig["options"] = originalOptions

		when:
		def options = ModuleOptions.get(parentConfig)

		then:
		options.is(originalOptions)
		parentConfig["options"].is(originalOptions)
	}

	void "add() works as expected"() {
		def options = new ModuleOptions()

		when:
		options.add(ModuleOption.createBoolean("a", true))
		options.add(ModuleOption.createString("b", "b"))
		options.add(ModuleOption.createInt("c", 0))

		options.add(ModuleOption.createString("b", "bbb"))

		then:
		options as Map == [
			a: [value: true, type: "boolean"],
			b: [value: "bbb", type: "string" ],
			c: [value: 0, type: "int"]
		]
	}

	void "addIfMissing() works as expected"() {
		def options = new ModuleOptions()

		when:
		options.addIfMissing(ModuleOption.createBoolean("a", true))
		options.addIfMissing(ModuleOption.createString("b", "b"))
		options.addIfMissing(ModuleOption.createInt("c", 0))

		options.addIfMissing(ModuleOption.createString("b", "bbb"))

		then:
		options as Map == [
			a: [value: true, type: "boolean"],
			b: [value: "b", type: "string" ],
			c: [value: 0, type: "int"]
		]
	}

	void "getOption() works as expected"() {
		def options = new ModuleOptions()

		when:
		options.addIfMissing(ModuleOption.createBoolean("a", true))
		options.addIfMissing(ModuleOption.createString("b", "b"))
		options.addIfMissing(ModuleOption.createInt("c", 0))

		then:
		options.getOption("a") as Map == [value: true, type: "boolean"]
		options.getOption("b") as Map == [value: "b", type: "string"]
		options.getOption("c") as Map == [value: 0, type: "int"]
		options.getOption("d") == null
	}
}
