package com.unifina.feed.cassandra;

import com.datastax.driver.core.*;
import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.utils.StreamPartition;
import com.unifina.service.CassandraService;
import grails.util.Holders;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

public class CassandraHistoricalIterator implements Iterator<StreamMessage>, Closeable {

	private final StreamPartition streamPartition;
	private final Date startDate;
	private final Date endDate;
	private final CassandraService cassandraService;

	private Session session;

	private ResultSet resultSet;

	private static final Logger log = Logger.getLogger(CassandraHistoricalIterator.class);

	private static final int PREFETCH_WHEN_REMAINING = 4500;
	private static final int FETCH_SIZE = 5000;

	public CassandraHistoricalIterator(StreamPartition streamPartition, Date startDate, Date endDate) {
		this.streamPartition = streamPartition;
		this.startDate = startDate;
		this.endDate = endDate;
		this.cassandraService = Holders.getApplicationContext().getBean(CassandraService.class);
		connect();
	}

	/**
	 * Returns a session with the Streamr Cassandra cluster.
	 */
	protected Session getSession() {
		return cassandraService.getSession();
	}

	private void connect() {
		session = getSession();
		session.getCluster().getConfiguration().getQueryOptions().setFetchSize(FETCH_SIZE);
		Statement s = new SimpleStatement("SELECT payload FROM stream_data WHERE id = ? AND partition = ? AND ts >= ? and ts <= ? ORDER BY ts ASC", streamPartition.getStreamId(), streamPartition.getPartition(), startDate, endDate);
		s.setIdempotent(true);
		resultSet = session.execute(s);
	}

	@Override
	public boolean hasNext() {
		return resultSet != null && !resultSet.isExhausted();
	}

	@Override
	public StreamMessage next() {
		Row row = resultSet.one();

		// Async-fetch more rows if not many left
		if (resultSet.getAvailableWithoutFetching() == PREFETCH_WHEN_REMAINING && !resultSet.isFullyFetched()) {
			log.info("Fetching more results.");
			resultSet.fetchMoreResults(); // this is asynchronous
		}
		try {
			return StreamMessage.fromBytes(row.getBytes("payload").array());
		} catch (IOException e) {
			log.error(e);
			return null;
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Unsupported!");
	}

	@Override
	public void close() throws IOException {
		// Don't close the session, because it's reusable
	}
}
