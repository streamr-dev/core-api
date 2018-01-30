package com.unifina.feed.twitter;

import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractStreamListener;

import java.util.HashMap;
import java.util.Map;

public class TwitterStreamListener implements AbstractStreamListener {

	@Override
	public void addToConfiguration(Map configuration, Stream stream) {
		if (!configuration.containsKey("twitter")) {
			configuration.put("twitter", new HashMap<>());
		}
	}

	@Override
	public void afterStreamSaved(Stream stream) {}

	@Override
	public void beforeDelete(Stream stream) {}
}
