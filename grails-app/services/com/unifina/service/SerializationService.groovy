package com.unifina.service

import com.unifina.serialization.SerializationException
import com.unifina.serialization.SerializerImpl
import com.unifina.signalpath.AbstractSignalPathModule

class SerializationService {

	def serializer = new SerializerImpl()

	String serialize(AbstractSignalPathModule module) throws SerializationException {
		module.beforeSerialization()
		serializer.serializeToString(module)
	}

	def deserialize(String data) throws SerializationException {
		AbstractSignalPathModule module = serializer.deserializeFromString(data)
		module.afterDeserialization()
		return module
	}

	def deserialize(String data, ClassLoader classLoader) throws SerializationException {
		AbstractSignalPathModule module = new SerializerImpl(classLoader).deserializeFromString(data)
		module.afterDeserialization()
		return module
	}
}