package com.unifina.signalpath.time

import spock.lang.Specification

class TimezoneParameterSpec extends Specification {
	TimezoneParameter p = new TimezoneParameter(null, "timezone", TimeZone.getTimeZone("Europe/Helsinki"))

	def "parse paris timezone"() {
		when:
		TimeZone result = p.parseValue("CET")
		then:
		result.getID() == "CET"
	}

	def "parse null value returns the default value"() {
		when:
		TimeZone result = p.parseValue(null)
		then:
		result.getID() == "Europe/Helsinki"
	}

	def "format value returns timezone as string"() {
		when:
		String result = p.formatValue(TimeZone.getTimeZone("GMT"))
		then:
		result == "GMT"
	}

	def "format null value returns parameters default"() {
		when:
		String result = p.formatValue(null)
		then:
		result == "Europe/Helsinki"
	}

	def "parseValue and formatValue are compatible"() {
		TimeZone tz = TimeZone.getTimeZone("Europe/Zurich")

		when:
		TimeZone result = p.parseValue(p.formatValue(tz).toString())
		then:
		result == tz
	}
}
