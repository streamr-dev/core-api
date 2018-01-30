package com.unifina.feed.cassandra;

import com.datastax.driver.core.Session;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractStreamListener;
import com.unifina.service.CassandraService;
import grails.util.Holders;

import java.util.Map;

/**
 * Connects to the Cassandra cluster configured in Grails config and deletes data when the Stream is deleted.
 */
public class CassandraDeletingStreamListener implements AbstractStreamListener {

	@Override
	public void addToConfiguration(Map configuration, Stream stream) {}

	@Override
	public void afterStreamSaved(Stream stream) {}

	protected Session getSession() {
		return Holders.getApplicationContext().getBean(CassandraService.class).getSession();
	}

	@Override
	public void beforeDelete(Stream stream) {
		Session session = getSession();
		for (int partition=0; partition < stream.getPartitions(); partition++) {
			session.execute("delete from stream_events where stream = ? and stream_partition = ?", stream.getId(), partition);
			session.execute("delete from stream_timestamps where stream = ? and stream_partition = ?", stream.getId(), partition);
		}
	}
}
