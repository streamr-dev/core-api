package com.unifina.serialization

import spock.lang.Specification

class SerializerImplSpec extends Specification {

	def serializer = new SerializerImpl()

	def "it throws SerializationException when de-serializing empty string"() {
		when:
		serializer.deserializeFromString("")

		then:
		thrown(SerializationException)
	}

	def "it throws SerializationException when de-serializing invalid string"() {
		when:
		serializer.deserializeFromString("{}")

		then:
		thrown(SerializationException)
	}
}