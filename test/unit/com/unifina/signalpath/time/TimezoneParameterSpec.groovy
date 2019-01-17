package com.unifina.signalpath.time

import spock.lang.Specification

class TimezoneParameterSpec extends Specification {
	TimezoneParameter p = new TimezoneParameter(null, "timezone", TimeZone.getTimeZone("Europe/Helsinki"))

	def "parse paris timezone"() {
		when:
		def result = p.parseValue("CET")
		then:
		result.getID() == "CET"
	}

	def "parse null value returns the default value"() {
		when:
		def result = p.parseValue(null)
		then:
		result.getID() == "Europe/Helsinki"
	}

	def "format value returns timezone as string"() {
		when:
		def result = p.formatValue(TimeZone.getTimeZone("GMT"))
		then:
		result.getID() == "GMT"
	}

	def "format null value returns parameters default"() {
		when:
		def result = p.formatValue(null)
		then:
		result.getID() == "Europe/Helsinki"
	}
}
