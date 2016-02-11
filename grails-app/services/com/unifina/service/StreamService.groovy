package com.unifina.service

import com.unifina.api.ValidationException
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.feed.AbstractStreamListener
import com.unifina.utils.IdGenerator
import grails.converters.JSON
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

	private AbstractStreamListener instantiateListener(Stream stream) {
		Assert.notNull(stream.feed.streamListenerClass, "feed's streamListenerClass is unexpectedly null")
		Class clazz = getClass().getClassLoader().loadClass(stream.feed.streamListenerClass)
		return clazz.newInstance(grailsApplication)
	}
}
