package com.unifina.service

import com.unifina.api.ValidationException
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.feed.AbstractDataRangeProvider
import com.unifina.feed.AbstractStreamListener
import com.unifina.feed.DataRange
import com.unifina.feed.FieldDetector
import com.unifina.utils.IdGenerator
import grails.converters.JSON
import org.springframework.util.Assert

class StreamService {

	def grailsApplication

	Stream findByName(String name) {
		return Stream.findByName(name)
	}
	
	Stream createStream(params, SecUser user) {
		Stream stream = new Stream(params)
		stream.id = IdGenerator.get()
		stream.apiKey = IdGenerator.get()
		stream.user = user
		stream.config = params.config

		// If no feed given, API feed is used
		if (stream.feed == null) {
			stream.feed = Feed.load(Feed.KAFKA_ID)
		}
		Map config = stream.getStreamConfigAsMap()
		if (!config.fields) {
			config.fields = []
		}
		AbstractStreamListener streamListener = instantiateListener(stream)
		streamListener.addToConfiguration(config, stream)
		stream.config = config as JSON

		if (!stream.validate()) {
			throw new ValidationException(stream.errors)
		}

		stream.save(failOnError: true)
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

	boolean autodetectFields(Stream stream, boolean flattenHierarchies) {
		FieldDetector fieldDetector = instantiateDetector(stream)
		fieldDetector.setFlattenMap(flattenHierarchies)
		def fields = fieldDetector?.detectFields(stream)
		if (fields) {
			Map config = stream.getStreamConfigAsMap()
			config.fields = fields
			stream.config = config as JSON
			stream.save(flush: true, failOnError: true)
			return true
		} else {
			return false
		}
	}

	// TODO: move to FeedService
	public AbstractStreamListener instantiateListener(Stream stream) {
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

	// TODO: move to FeedService
	private AbstractDataRangeProvider instantiateDataRangeProvider(Stream stream) {
		if (stream.feed.dataRangeProviderClass == null) {
			return null
		} else {
			Class clazz = getClass().getClassLoader().loadClass(stream.feed.dataRangeProviderClass)
			return clazz.newInstance(grailsApplication)
		}
	}

	DataRange getDataRange(Stream stream) {
		AbstractDataRangeProvider provider = instantiateDataRangeProvider(stream)
		if (provider)
			return provider.getDataRange(stream)
		else return null
	}
}
