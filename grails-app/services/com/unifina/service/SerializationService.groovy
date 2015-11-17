package com.unifina.service

import com.unifina.serialization.SerializationException
import com.unifina.serialization.SerializerImpl

class SerializationService {

	def serializer = new SerializerImpl()

	String serialize(Object object) throws SerializationException {
		serializer.serializeToString(object)
	}

	def deserialize(String data) throws SerializationException {
		serializer.deserializeFromString(data)
	}
}