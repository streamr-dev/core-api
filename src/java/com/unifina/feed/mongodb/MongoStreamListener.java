package com.unifina.feed.mongodb;

import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractStreamListener;

import java.util.HashMap;
import java.util.Map;

public class MongoStreamListener implements AbstractStreamListener {

	@Override
	public void addToConfiguration(Map configuration, Stream stream) {
		if (!configuration.containsKey("mongodb")) {
			configuration.put("mongodb", new HashMap<>());
		}
	}

	@Override
	public void afterStreamSaved(Stream stream) {}

	@Override
	public void beforeDelete(Stream stream) {}
}
