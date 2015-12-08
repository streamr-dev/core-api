package com.unifina.service

import com.unifina.serialization.SerializationException
import com.unifina.serialization.SerializerImpl
import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.utils.MapTraversal
import org.springframework.util.Assert

class SerializationService {

	final static String INTERVAL_CONFIG_KEY = "unifina.serialization.intervalInMillis"

	def grailsApplication
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

	public Long serializationIntervalInMillis() {
		Long v = MapTraversal.getLong(grailsApplication.getConfig(), INTERVAL_CONFIG_KEY);
		Assert.notNull(v, "Missing key \"" + INTERVAL_CONFIG_KEY + "\" from grailsApplication configuration");
		return v;
	}
}