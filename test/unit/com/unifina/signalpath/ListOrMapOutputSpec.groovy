package com.unifina.signalpath

import com.unifina.signalpath.simplemath.Multiply
import spock.lang.Specification

class ListOrMapOutputSpec extends Specification {
	ListOrMapInput input = new ListOrMapInput(new Multiply(), "in")
	ListOrMapOutput output = new ListOrMapOutput(null, "out", input)

	def "sends list with nulls removed, if linked input previously received list"() {
		input.receive([1, 2, 3, 4, 5, 6])

		when:
		output.send(["a", "b", null, null, "c", "d"])
		then:
		output.previousValue == ["a", "b", "c", "d"]
	}

	def "sends map with null entries removed, if linked input previously received map"() {
		input.receive([a: "a", b: "b", c: "c", d: "d", e: "e", f: "f"])

		when:
		output.send([1, 2, 3, null, 5, null])
		then:
		output.previousValue == [e: 5, b: 2, c: 3, a: 1]
	}
}
