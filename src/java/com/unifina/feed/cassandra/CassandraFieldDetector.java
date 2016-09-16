package com.unifina.feed.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.unifina.data.StreamrBinaryMessage;
import com.unifina.domain.data.Stream;
import com.unifina.feed.FieldDetector;
import com.unifina.feed.StreamrBinaryMessageParser;
import com.unifina.feed.map.MapMessage;
import com.unifina.service.CassandraService;
import org.codehaus.groovy.grails.commons.GrailsApplication;

/**
 * Creates a new Cassandra Session that connects to the Cluster defined in the Stream, and selects the latest message.
 * The connection is closed in the end.
 */
public class CassandraFieldDetector extends FieldDetector {

	public CassandraFieldDetector(GrailsApplication grailsApplication) {
		super(grailsApplication);
	}

	@Override
	protected MapMessage fetchExampleMessage(Stream stream) {
		Session session = null;
		try {
			session = getSession(stream);
			ResultSet resultSet = session.execute("SELECT payload FROM stream_events WHERE stream = ? ORDER BY kafka_offset DESC LIMIT 1", stream.getId());
			Row row = resultSet.one();
			if (row != null) {
				StreamrBinaryMessageParser parser = new StreamrBinaryMessageParser();
				return parser.parse(new StreamrBinaryMessage(row.getBytes("payload")));
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
		// don't close the reusable session
	}
}
