package com.unifina.service

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.feed.AbstractStreamListener
import com.unifina.utils.IdGenerator
import grails.converters.JSON

class StreamService {

	def grailsApplication
	def kafkaService
	def feedFileService
	
	Stream createStream(params, SecUser user, fields) {
		Stream stream = new Stream(params)
		stream.uuid = IdGenerator.get()
		stream.apiKey = IdGenerator.get()
		stream.user = user

		Class clazz = getClass().getClassLoader().loadClass(stream.feed.streamListenerClass)
		AbstractStreamListener streamListener = clazz.newInstance(grailsApplication)
		Map config = [fields: fields != null ? fields : []]
		streamListener.addToConfiguration(config, stream)
		stream.config = config as JSON

		stream.save()
		streamListener.afterStreamSaved(stream)
		return stream
	}
	
	void deleteStream(Stream stream) {
		if (stream.feed.id==Feed.KAFKA_ID) {
			def feedFiles = FeedFile.findAllByStream(stream, [sort:'beginDate'])
			feedFiles.each {
				feedFileService.deleteFile(it)
			}
			FeedFile.executeUpdate("delete from FeedFile ff where ff.stream = :stream", [stream: stream])
			kafkaService.deleteTopics([stream.uuid])
			stream.delete(flush:true)
		}
		else throw new RuntimeException("Unable to delete stream $stream.id, feed: $stream.feed.id")
	}
}
