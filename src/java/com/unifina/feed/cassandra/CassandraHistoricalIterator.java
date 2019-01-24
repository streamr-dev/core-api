package com.unifina.feed.cassandra;

import com.datastax.driver.core.*;
import com.streamr.client.protocol.message_layer.StreamMessage;
import com.unifina.domain.data.Stream;
import com.unifina.feed.StreamrMessageParser;
import com.unifina.feed.map.MapMessage;
import com.unifina.service.CassandraService;
import grails.util.Holders;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

public class CassandraHistoricalIterator implements Iterator<MapMessage>, Closeable {

	private final Stream stream;
	private final Integer partition;
	private final Date startDate;
	private final Date endDate;
	private final CassandraService cassandraService;

	private Session session;

	private ResultSet resultSet;

	private static final Logger log = Logger.getLogger(CassandraHistoricalIterator.class);

	private static final int PREFETCH_WHEN_REMAINING = 4500;
	private static final int FETCH_SIZE = 5000;

	private StreamrMessageParser parser = new StreamrMessageParser();

	public CassandraHistoricalIterator(Stream stream, Integer partition, Date startDate, Date endDate) {
		this.stream = stream;
		this.partition = partition;
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
		Statement s = new SimpleStatement("SELECT payload FROM stream_data WHERE id = ? AND partition = ? AND ts >= ? and ts <= ? ORDER BY ts ASC", stream.getId(), partition, startDate, endDate);
		s.setIdempotent(true);
		resultSet = session.execute(s);
	}

	@Override
	public boolean hasNext() {
		return resultSet != null && !resultSet.isExhausted();
	}

	@Override
	public MapMessage next() {
		Row row = resultSet.one();

		// Async-fetch more rows if not many left
		if (resultSet.getAvailableWithoutFetching() == PREFETCH_WHEN_REMAINING && !resultSet.isFullyFetched()) {
			log.info("Fetching more results.");
			resultSet.fetchMoreResults(); // this is asynchronous
		}
		try {
			StreamMessage msg = StreamMessage.fromBytes(row.getBytes("payload").array());
			return parser.parse(msg);
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
