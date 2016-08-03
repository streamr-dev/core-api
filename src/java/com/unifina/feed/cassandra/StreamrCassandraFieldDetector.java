package com.unifina.feed.cassandra;

import com.datastax.driver.core.Session;
import com.unifina.domain.data.Stream;
import com.unifina.service.CassandraService;
import org.codehaus.groovy.grails.commons.GrailsApplication;

/**
 * Creates a new Cassandra Session that connects to the Streamr Cassandra cluster, and selects the latest message.
 */
public class StreamrCassandraFieldDetector extends CassandraFieldDetector {

	public StreamrCassandraFieldDetector(GrailsApplication grailsApplication) {
		super(grailsApplication);
	}

	@Override
	protected Session getSession(Stream stream) {
		return grailsApplication.getMainContext().getBean(CassandraService.class).getSession();
	}

	@Override
	protected void cleanup(Session session) {
		// don't close the reusable session
	}
}
