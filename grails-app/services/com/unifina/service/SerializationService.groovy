package com.unifina.service

import com.unifina.serialization.SerializationException
import com.unifina.serialization.Serializer
import com.unifina.serialization.SerializerImpl
import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.utils.MapTraversal
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.util.Assert

@CompileStatic
class SerializationService {

	final static String INTERVAL_CONFIG_KEY = "streamr.serialization.intervalInMillis"
	final static String MAX_BYTES_CONFIG_KEY = "streamr.serialization.maxBytes"

	GrailsApplication grailsApplication
	Serializer serializer = new SerializerImpl()

	byte[] serialize(AbstractSignalPathModule module) throws SerializationException {
		module.beforeSerialization()
		byte[] result = serializer.serializeToByteArray(module)
		module.afterSerialization()
		return result
	}

	AbstractSignalPathModule deserialize(byte[] data) throws SerializationException {
		AbstractSignalPathModule module = (AbstractSignalPathModule) serializer.deserializeFromByteArray(data)
		module.afterDeserialization(this)
		return module
	}

	AbstractSignalPathModule deserialize(byte[] data, ClassLoader classLoader) throws SerializationException {
		AbstractSignalPathModule module = (AbstractSignalPathModule) new SerializerImpl(classLoader).deserializeFromByteArray(data)
		module.afterDeserialization(this)
		return module
	}

	Long serializationIntervalInMillis() {
		Long v = MapTraversal.getLong(grailsApplication.getConfig(), INTERVAL_CONFIG_KEY)
		Assert.notNull(v, "Missing key \"" + INTERVAL_CONFIG_KEY + "\" from grailsApplication configuration")
		return v
	}

	int serializationMaxBytes() {
		Integer v = MapTraversal.getLong(grailsApplication.getConfig(), MAX_BYTES_CONFIG_KEY)
		Assert.notNull(v, "Missing key \"" + MAX_BYTES_CONFIG_KEY + "\" from grailsApplication configuration")
		return v
	}
}