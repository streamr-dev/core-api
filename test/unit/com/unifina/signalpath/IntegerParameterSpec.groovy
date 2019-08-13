package com.unifina.signalpath

import com.unifina.signalpath.utils.Label
import spock.lang.Specification

class IntegerParameterSpec extends Specification {
	void "test parse empty string"() {
		setup:
		Label label = new Label()
		label.setDisplayName("foobar")
		label.init()
		IntegerParameter parameter = new IntegerParameter(label, "int-param", 0)
		when:
		parameter.parseValue("")
		then:
		def e = thrown(RuntimeException)
		e.message == "Module foobar's parameter 'int-param' cannot parse value ''"

	}
}
