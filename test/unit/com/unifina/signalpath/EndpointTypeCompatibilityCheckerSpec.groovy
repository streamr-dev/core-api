package com.unifina.signalpath

import spock.lang.Specification
import spock.lang.Unroll

class EndpointTypeCompatibilityCheckerSpec extends Specification {
	@Unroll
	def "#t1 is compatible with #t2"(String t1, String t2) {
		Input in1 = new Input(null, "in1", t1)
		Input in2 = new Input(null, "in2", t2)

		expect:
		EndpointTypeCompatibilityChecker.areCompatible(in1, in2)

		where:
		t1              | t2
		"String"        | "String"
		"Double String" | "String"
		"String"        | "String Stream"
		"Object"        | "Boolean Number"
		"String Stream" | "Object"
		"Object"        | "Object"
	}

	@Unroll
	def "#t1 is not compatible with #t2"(String t1, String t2) {
		Input in1 = new Input(null, "in1", t1)
		Input in2 = new Input(null, "in2", t2)

		expect:
		!EndpointTypeCompatibilityChecker.areCompatible(in1, in2)

		where:
		t1               | t2
		"String"         | "Double"
		"Double Boolean" | "String"
		"Boolean"        | "Double String Stream"
	}
}
