package com.unifina.serialization

import org.codehaus.groovy.grails.web.json.JSONObject
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

	def "serializing Map inside JSONObject"() {
		JSONObject object = new JSONObject()
		object.put("foo", "bar")
		object.put("options", new HashMap<String, Object>())

		when:
		def bytes = serializer.serializeToByteArray(object)
		def deserialized = serializer.deserializeFromByteArray(bytes)

		then:
		deserialized.foo == "bar"
		deserialized.options instanceof Map
	}
}