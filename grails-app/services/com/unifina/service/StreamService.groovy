package com.unifina.service

import grails.converters.JSON

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.utils.IdGenerator

class StreamService {
	
	def kafkaService
	def feedFileService
	
	Stream createUserStream(params, SecUser user) {
		Stream stream = new Stream(params)
		stream.uuid = IdGenerator.get()
		stream.apiKey = IdGenerator.get()
		stream.user = user
		if (stream.localId==null)
			stream.localId = stream.name
		
		stream.feed = Feed.load(7L) // API stream
		stream.streamConfig = ([fields:[], topic: stream.uuid] as JSON)
		
		stream.save()
		
		if (!stream.hasErrors()) {
			kafkaService.createTopics([stream.uuid])
		}
		return stream
	}
	
	void deleteStream(Stream stream) {
		if (stream.feed.id==7) {
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
