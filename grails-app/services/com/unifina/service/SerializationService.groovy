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

	final static String INTERVAL_CONFIG_KEY = "unifina.serialization.intervalInMillis"

	GrailsApplication grailsApplication
	Serializer serializer = new SerializerImpl()

	String serialize(AbstractSignalPathModule module) throws SerializationException {
		module.beforeSerialization()
		String result = serializer.serializeToString(module)
		module.afterSerialization()
		return result
	}

	AbstractSignalPathModule deserialize(String data) throws SerializationException {
		AbstractSignalPathModule module = (AbstractSignalPathModule) serializer.deserializeFromString(data)
		module.afterDeserialization()
		return module
	}

	AbstractSignalPathModule deserialize(String data, ClassLoader classLoader) throws SerializationException {
		AbstractSignalPathModule module = (AbstractSignalPathModule) new SerializerImpl(classLoader).deserializeFromString(data)
		module.afterDeserialization()
		return module
	}

	public Long serializationIntervalInMillis() {
		Long v = MapTraversal.getLong(grailsApplication.getConfig(), INTERVAL_CONFIG_KEY);
		Assert.notNull(v, "Missing key \"" + INTERVAL_CONFIG_KEY + "\" from grailsApplication configuration");
		return v;
	}
}