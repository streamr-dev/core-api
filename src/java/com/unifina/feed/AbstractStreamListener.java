package com.unifina.feed;

import com.unifina.domain.data.Stream;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import java.util.Map;

public abstract class AbstractStreamListener {

	public AbstractStreamListener(GrailsApplication grailsApplication) {}

	public abstract void addToConfiguration(Map configuration, Stream stream);

	public abstract void afterStreamSaved(Stream stream);
}
