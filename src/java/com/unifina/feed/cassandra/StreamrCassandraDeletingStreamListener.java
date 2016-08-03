package com.unifina.feed.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractStreamListener;
import com.unifina.utils.MapTraversal;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import java.util.Map;

/**
 * Connects to the Cassandra cluster configured in Grails config and deletes data when the Stream is deleted.
 */
public class StreamrCassandraDeletingStreamListener extends AbstractStreamListener {

	GrailsApplication grails;

	public StreamrCassandraDeletingStreamListener(GrailsApplication grailsApplication) {
		super(grailsApplication);
		this.grails = grailsApplication;
	}

	@Override
	public void addToConfiguration(Map configuration, Stream stream) {

	}

	@Override
	public void afterStreamSaved(Stream stream) {

	}

	@Override
	public void beforeDelete(Stream stream) {
		Map<String, Object> cassandraSubConfig = MapTraversal.getMap(grails.getConfig(), "streamr.cassandra");
		CassandraConfig config = new CassandraConfig(cassandraSubConfig);

		Cluster cluster = null;
		try {
			Cluster.Builder builder = Cluster.builder();
			for (String host : config.getHosts()) {
				builder.addContactPoint(host);
			}
			cluster = builder.build();
			Session session = cluster.connect(config.getKeySpace());

			session.execute("delete from stream_events where stream = ?", stream.getId());
		} finally {
			if (cluster != null) {
				cluster.close();
			}
		}
	}
}
