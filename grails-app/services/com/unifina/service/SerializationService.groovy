package com.unifina.service

import com.unifina.serialization.SerializerImpl

class SerializationService {

	def serializer = new SerializerImpl()

	String serialize(Object object) {
		serializer.serializeToString(object)
	}

	def deserialize(String data) {
		serializer.deserializeFromString(data)
	}
}