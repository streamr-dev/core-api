package com.unifina.service

import com.unifina.api.ValidationException
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.feed.AbstractStreamListener
import com.unifina.feed.FieldDetector
import com.unifina.utils.IdGenerator
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.parser.JSONParser
import org.springframework.util.Assert

class StreamService {

	def grailsApplication
	
	Stream createStream(params, SecUser user, fields) {
		Stream stream = new Stream(params)
		stream.uuid = IdGenerator.get()
		stream.apiKey = IdGenerator.get()
		stream.user = user

		AbstractStreamListener streamListener
		if (stream.feed != null) {
			streamListener = instantiateListener(stream)
			Map config = [fields: fields != null ? fields : []]
			streamListener.addToConfiguration(config, stream)
			stream.config = config as JSON
		}

		if (!stream.validate()) {
			throw new ValidationException(stream.errors)
		}

		stream.save()
		if (streamListener) {
			streamListener.afterStreamSaved(stream)
		}
		return stream
	}
	
	void deleteStream(Stream stream) {
		AbstractStreamListener streamListener = instantiateListener(stream)
		streamListener.beforeDelete(stream)
		stream.delete(flush:true)
	}

	boolean autodetectFields(Stream stream) {
		FieldDetector fieldDetector = instantiateDetector(stream)
		if (fieldDetector == null) {
			return false
		} else {
			def fields = fieldDetector.detectFields(stream)
			Map config = JSON.parse(stream.config)
			config.fields = fields
			stream.config = config as JSON
			return true
		}
	}

	// TODO: move to FeedService
	private AbstractStreamListener instantiateListener(Stream stream) {
		Assert.notNull(stream.feed.streamListenerClass, "feed's streamListenerClass is unexpectedly null")
		Class clazz = getClass().getClassLoader().loadClass(stream.feed.streamListenerClass)
		return clazz.newInstance(grailsApplication)
	}

	// TODO: move to FeedService
	private FieldDetector instantiateDetector(Stream stream) {
		if (stream.feed.fieldDetectorClass == null) {
			return null
		} else {
			Class clazz = getClass().getClassLoader().loadClass(stream.feed.fieldDetectorClass)
			return clazz.newInstance(grailsApplication)
		}

	}
}
