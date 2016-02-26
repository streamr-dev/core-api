package com.unifina.feed;

import com.unifina.domain.data.Stream;
import org.codehaus.groovy.grails.commons.GrailsApplication;

/**
 * Created by henripihkala on 25/02/16.
 */
public abstract class AbstractDataRangeProvider {
	private final GrailsApplication grailsApplication;

	public AbstractDataRangeProvider(GrailsApplication grailsApplication) {
		this.grailsApplication = grailsApplication;
	}

	public abstract DataRange getDataRange(Stream stream);
}
