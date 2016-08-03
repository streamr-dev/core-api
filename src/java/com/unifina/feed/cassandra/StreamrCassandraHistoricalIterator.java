package com.unifina.feed.cassandra;

import com.datastax.driver.core.Session;
import com.unifina.domain.data.Stream;
import com.unifina.service.CassandraService;
import com.unifina.utils.MapTraversal;
import grails.util.Holders;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * Like CassandraHistoricalIterator, but creates CassandraConfig from Grails config instead of
 * Stream config. Used to connect to the Streamr Cassandra cluster instead of a user-defined one.
 *
 * Also does not close the Cluster when the iteration ends.
 */
public class StreamrCassandraHistoricalIterator extends CassandraHistoricalIterator {

	public StreamrCassandraHistoricalIterator(Stream stream, Date startDate, Date endDate) {
		super(stream, startDate, endDate);
	}

	/**
	 * Returns a session with the Streamr Cassandra cluster.
     */
	@Override
	protected Session getSession() {
		return Holders.getApplicationContext().getBean(CassandraService.class).getSession();
	}

	@Override
	public void close() throws IOException {
		// Don't close the session, because it's reusable
	}
}
