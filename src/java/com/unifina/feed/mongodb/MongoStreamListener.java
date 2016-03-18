package com.unifina.feed.mongodb;

import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractStreamListener;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import java.util.HashMap;
import java.util.Map;

public class MongoStreamListener extends AbstractStreamListener {
	public MongoStreamListener(GrailsApplication grailsApplication) {
		super(grailsApplication);
	}

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
