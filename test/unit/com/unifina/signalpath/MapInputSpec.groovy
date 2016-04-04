package com.unifina.signalpath

import com.unifina.signalpath.utils.Label
import spock.lang.Specification
import spock.lang.Unroll

class MapInputSpec extends Specification {
	MapInput input = new MapInput(new Label(), "output")

	def "result of getValue() is not modifiable"() {
		// In practice, all maps are UnmodifiableMaps, see Output.send
		input.receive(Collections.unmodifiableMap([a: "a"]))

		when:
		Map m = input.getValue()
		m["hello"] = "world"

		then:
		thrown(UnsupportedOperationException)
	}

	def "result of getModifiableValue() is modifiable"() {
		input.receive(Collections.unmodifiableMap([a: "a"]))

		when:
		Map m = input.getModifiableValue()
		m["hello"] = "world"

		then:
		m == [a: "a", hello: "world"]
	}
}
