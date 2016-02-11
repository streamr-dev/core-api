package com.unifina.feed;

import com.unifina.domain.data.Stream;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import java.util.Map;

/**
 * When <code>Stream</code>s are either created and destroyed, this class handles its <code>Feed</code>-specific logic.
 */
public abstract class AbstractStreamListener {

	public AbstractStreamListener(GrailsApplication grailsApplication) {}

	/**
	 * Extend a newly instantiated Stream's configuration with logic.
	 * @param configuration the configuration of the Stream
	 * @param stream the newly instantiated (but not yet persisted) Stream
	 */
	public abstract void addToConfiguration(Map configuration, Stream stream);

	/**
	 * Perform some logic after Stream has been persisted.
	 * @param stream the persisted Stream
	 */
	public abstract void afterStreamSaved(Stream stream);

	/**
	 * Perform some logic (e.g. cleaning up) before Stream is destroyed.
	 * @param stream stream to be unpersisted
	 */
	public abstract void beforeDelete(Stream stream);
}
