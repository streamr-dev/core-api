package com.unifina.serialization

import spock.lang.Specification

/**
 * The fast-serialization library has problems dealing with NaN and infinity values. In this test we ensure that our
 * own hack (see <code>SerializerImpl<code>) deals with these boundary values appropriately and that normal values still
 * work properly.
 */
class DoubleSerializationSpec extends Specification {

	def serializer = new SerializerImpl()

	def "it properly serializes/deserializes double"(double val, def notused) {
		when:
		String serialized = serializer.serializeToString(val)
		double newObj = serializer.deserializeFromString(serialized)

		then:
		newObj == val

		where:
		val 						| notused
		0  							| _
		-3.141592 					| _
		3.141592 					| _
		Double.NaN 					| _
		Double.POSITIVE_INFINITY	| _
		Double.NEGATIVE_INFINITY	| _
	}

	def "it properly serializes/deserializes Double"(double val, def notused) {
		when:
		String serialized = serializer.serializeToString(Double.valueOf(val))
		def newObj = serializer.deserializeFromString(serialized)

		then:
		newObj.equals(val)

		where:
		val 						| notused
		0  							| _
		-3.141592 					| _
		3.141592 					| _
		Double.NaN 					| _
		Double.POSITIVE_INFINITY	| _
		Double.NEGATIVE_INFINITY	| _
	}
}