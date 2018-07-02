package com.unifina.feed.kafka

import com.unifina.domain.data.Stream
import com.unifina.feed.AbstractStreamListener
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
	void beforeDelete(Stream stream) {}
}
