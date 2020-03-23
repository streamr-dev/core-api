package com.unifina.utils


import spock.lang.Specification

class PropertiesUtilSpec extends Specification {

	def "matchingPropertiesToMap()"() {
		Properties properties = new Properties()
		properties.putAll(["foo.bar": 1, "foo.foo": 2, "xyz": 3])

		expect:
		PropertiesUtil.matchingPropertiesToMap("foo.", properties) == [bar: 1, foo: 2]
	}

}
