package com.unifina.feed.kafka;

import com.unifina.domain.data.Stream;
import com.unifina.feed.FieldDetector;
import com.unifina.feed.map.MapMessage;

public class KafkaFieldDetector extends FieldDetector {
	@Override
	protected MapMessage fetchExampleMessage(Stream stream) {
		// TODO: ole hyvä, henri
		return null;
	}
}
