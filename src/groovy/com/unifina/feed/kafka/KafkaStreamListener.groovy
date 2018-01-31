package com.unifina.feed.kafka

import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.feed.AbstractStreamListener
import com.unifina.service.FeedFileService
import grails.util.Holders
import groovy.transform.CompileStatic

class KafkaStreamListener implements AbstractStreamListener {

	@Override
	@CompileStatic
	void addToConfiguration(Map configuration, Stream stream) {
		configuration["topic"] = stream.id
	}

	@Override
	@CompileStatic
	void afterStreamSaved(Stream stream) {}

	@Override
	void beforeDelete(Stream stream) {
		FeedFileService feedFileService = Holders.getApplicationContext().getBean(FeedFileService.class)
		def feedFiles = FeedFile.findAllByStream(stream, [sort:'beginDate'])
		feedFiles.each {
			feedFileService.deleteFile(it)
		}
		FeedFile.executeUpdate("delete from FeedFile ff where ff.stream = :stream", [stream: stream])
	}
}
