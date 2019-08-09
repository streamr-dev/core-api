package com.unifina.signalpath

import com.unifina.signalpath.utils.Label
import spock.lang.Specification

class DoubleParameterSpec extends Specification {
	void "test parse empty string"() {
		setup:
		Label label = new Label()
		label.setDisplayName("foobar")
		label.init()
		DoubleParameter parameter = new DoubleParameter(label, "double-param", 0.0)
		when:
		parameter.parseValue("")
		then:
		def e = thrown(RuntimeException)
		e.message == "Module foobar's parameter 'double-param' cannot parse value ''"
	}
}
