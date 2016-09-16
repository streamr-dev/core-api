package com.unifina.feed.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.unifina.data.StreamrBinaryMessage;
import com.unifina.domain.data.FeedFile;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractDataRangeProvider;
import com.unifina.feed.DataRange;
import com.unifina.feed.StreamrBinaryMessageParser;
import com.unifina.service.CassandraService;
import com.unifina.service.FeedFileService;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import java.util.Date;

/**
 * Reads the timestamp of the first and last rows in the Cassandra cluster defined by the given Stream.
 */
public class CassandraDataRangeProvider extends AbstractDataRangeProvider {

	public CassandraDataRangeProvider(GrailsApplication grailsApplication) {
		super(grailsApplication);
	}

	@Override
	public DataRange getDataRange(Stream stream) {
		Session session = null;
		try {
			session = getSession(stream);
			ResultSet resultSet = session.execute("SELECT ts FROM stream_events WHERE stream = ? ORDER BY kafka_offset ASC LIMIT 1", stream.getId());
			Row firstRow = resultSet.one();
			if (firstRow != null) {
				Date firstTimestamp = firstRow.getTimestamp("ts");
				resultSet = session.execute("SELECT ts FROM stream_events WHERE stream = ? ORDER BY kafka_offset DESC LIMIT 1", stream.getId());
				Row lastRow = resultSet.one();
				Date lastTimestamp = lastRow.getTimestamp("ts");
				return new DataRange(firstTimestamp, lastTimestamp);
			}
			else return null;
		} finally {
			if (session != null) {
				cleanup(session);
			}
		}
	}

	protected Session getSession(Stream stream) {
		return grailsApplication.getMainContext().getBean(CassandraService.class).getSession();
	}

	protected void cleanup(Session session) {
		// Don't close the session, it's reusable
	}
}
