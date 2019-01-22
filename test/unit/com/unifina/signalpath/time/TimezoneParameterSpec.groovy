package com.unifina.signalpath.time

import spock.lang.Specification

class TimezoneParameterSpec extends Specification {
	TimezoneParameter p = new TimezoneParameter(null, "timezone", "Europe/Helsinki")

	def "parse paris timezone"() {
		when:
		String result = p.parseValue("CET")
		then:
		result == "CET"
	}

	def "parse null value returns the default value"() {
		when:
		String result = p.parseValue(null)
		then:
		result == "Europe/Helsinki"
	}

	def "format value returns timezone as string"() {
		when:
		String result = p.formatValue("GMT")
		then:
		result == "GMT"
	}

	def "format null value returns parameters default"() {
		when:
		String result = p.formatValue(null)
		then:
		result == "Europe/Helsinki"
	}
}
