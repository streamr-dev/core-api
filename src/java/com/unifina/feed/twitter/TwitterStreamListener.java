package com.unifina.feed.twitter;

import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractStreamListener;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import java.util.HashMap;
import java.util.Map;

public class TwitterStreamListener extends AbstractStreamListener {
	public TwitterStreamListener(GrailsApplication grailsApplication) { super(grailsApplication); }

	@Override
	public void addToConfiguration(Map configuration, Stream stream) {
		if (!configuration.containsKey("twitter")) {
			configuration.put("twitter", new HashMap<>());
		}
	}

	@Override
	public void afterStreamSaved(Stream stream) {

	}

	@Override
	public void beforeDelete(Stream stream) {

	}
}
