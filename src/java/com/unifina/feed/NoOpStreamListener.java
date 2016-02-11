package com.unifina.feed;

import com.unifina.domain.data.Stream;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import java.util.Map;

public class NoOpStreamListener extends AbstractStreamListener {
	public NoOpStreamListener(GrailsApplication grailsApplication) {
		super(grailsApplication);
	}

	@Override
	public void addToConfiguration(Map configuration, Stream stream) {}

	@Override
	public void afterStreamSaved(Stream stream) {}

	@Override
	public void beforeDelete(Stream stream) {
	}
}
