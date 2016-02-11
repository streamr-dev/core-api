package com.unifina.feed.kafka;

import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractStreamListener;
import com.unifina.service.KafkaService;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import java.util.Arrays;
import java.util.Map;

public class KafkaStreamListener extends AbstractStreamListener {

	private final KafkaService kafkaService;

	public KafkaStreamListener(GrailsApplication grailsApplication) {
		super(grailsApplication);
		kafkaService = grailsApplication.getMainContext().getBean(KafkaService.class);
	}

	@Override
	public void addToConfiguration(Map configuration, Stream stream) {
		configuration.put("topic", stream.getUuid());
	}

	@Override
	public void afterStreamSaved(Stream stream) {
		if (!stream.hasErrors()) {
			kafkaService.createTopics(Arrays.asList(stream.getUuid()));
		}
	}
}
