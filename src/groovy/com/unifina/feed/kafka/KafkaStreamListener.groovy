package com.unifina.feed.kafka;

import com.unifina.domain.data.FeedFile;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractStreamListener;
import com.unifina.service.FeedFileService;
import com.unifina.service.KafkaService
import groovy.transform.CompileStatic;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import java.util.Arrays;
import java.util.Map;

public class KafkaStreamListener extends AbstractStreamListener {

	private final KafkaService kafkaService
	private final FeedFileService feedFileService

	public KafkaStreamListener(GrailsApplication grailsApplication) {
		super(grailsApplication)
		kafkaService = grailsApplication.getMainContext().getBean(KafkaService.class)
		feedFileService = grailsApplication.getMainContext().getBean(FeedFileService.class)
	}

	@Override
	@CompileStatic
	public void addToConfiguration(Map configuration, Stream stream) {
		configuration["topic"] = stream.id
	}

	@Override
	@CompileStatic
	public void afterStreamSaved(Stream stream) {
		if (!stream.hasErrors()) {
			kafkaService.createTopics([stream.id]);
		}
	}

	@Override
	public void beforeDelete(Stream stream) {
		def feedFiles = FeedFile.findAllByStream(stream, [sort:'beginDate'])
		feedFiles.each {
			feedFileService.deleteFile(it)
		}
		FeedFile.executeUpdate("delete from FeedFile ff where ff.stream = :stream", [stream: stream])
		kafkaService.deleteTopics([stream.id])
	}
}
