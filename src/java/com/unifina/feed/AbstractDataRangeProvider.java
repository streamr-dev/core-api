package com.unifina.feed;

import com.unifina.domain.data.Stream;
import org.codehaus.groovy.grails.commons.GrailsApplication;

public abstract class AbstractDataRangeProvider {
	protected final GrailsApplication grailsApplication;

	public AbstractDataRangeProvider(GrailsApplication grailsApplication) {
		this.grailsApplication = grailsApplication;
	}

	public abstract DataRange getDataRange(Stream stream);
}
