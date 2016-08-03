package com.unifina.feed.cassandra;

import com.datastax.driver.core.Session;
import com.unifina.domain.data.Stream;
import com.unifina.service.CassandraService;
import org.codehaus.groovy.grails.commons.GrailsApplication;

/**
 * Reads the timestamp of the first and last rows in the Streamr Cassandra cluster defined in Grails config.
 */
public class StreamrCassandraDataRangeProvider extends CassandraDataRangeProvider {

	public StreamrCassandraDataRangeProvider(GrailsApplication grailsApplication) {
		super(grailsApplication);
	}

	@Override
	protected Session getSession(Stream stream) {
		return grailsApplication.getMainContext().getBean(CassandraService.class).getSession();
	}

	@Override
	protected void cleanup(Session session) {
		// Don't close the session, it's reusable
	}
}
