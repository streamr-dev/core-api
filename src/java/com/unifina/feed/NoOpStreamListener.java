package com.unifina.feed;

import com.unifina.domain.data.Stream;

import java.util.Map;

public class NoOpStreamListener implements AbstractStreamListener {

	@Override
	public void addToConfiguration(Map configuration, Stream stream) {}

	@Override
	public void afterStreamSaved(Stream stream) {}

	@Override
	public void beforeDelete(Stream stream) {}
}
