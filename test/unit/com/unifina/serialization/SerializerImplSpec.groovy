package com.unifina.serialization

import spock.lang.Specification

class SerializerImplSpec extends Specification {

	def serializer = new SerializerImpl()

	def "it throws SerializationException when de-serializing empty string"() {
		when:
		serializer.deserializeFromByteArray(new byte[0])

		then:
		thrown(SerializationException)
	}

	def "it throws SerializationException when de-serializing invalid string"() {
		when:
		serializer.deserializeFromByteArray("asdfasdfasfdasf".getBytes("UTF-8"))

		then:
		thrown(SerializationException)
	}
}