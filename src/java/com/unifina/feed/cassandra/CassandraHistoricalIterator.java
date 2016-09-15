package com.unifina.feed.cassandra;

import com.datastax.driver.core.*;
import com.unifina.data.StreamrBinaryMessage;
import com.unifina.domain.data.Stream;
import com.unifina.feed.map.JSONMessageParser;
import com.unifina.feed.map.MapMessage;
import grails.converters.JSON;
import org.codehaus.groovy.grails.web.json.JSONObject;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

public class CassandraHistoricalIterator implements Iterator<MapMessage>, Closeable {

	private final Stream stream;
	private final Date startDate;
	private final Date endDate;

	private Session session;

	private ResultSet resultSet;
	private Iterator<Row> resultSetIterator;

	private static final int PREFETCH_WHEN_REMAINING = 500;
	private static final int FETCH_SIZE = 5000;

	public CassandraHistoricalIterator(Stream stream, Date startDate, Date endDate) {
		this.stream = stream;
		this.startDate = startDate;
		this.endDate = endDate;
		connect();
	}

	/**
	 * Creates a new Cluster and Session configured by the given Stream.
     */
	protected Session getSession() {
		CassandraConfig config = new CassandraConfig(stream.getStreamConfigAsMap());
		return config.createSession();
	}


	private void connect() {
		session = getSession();
		session.getCluster().getConfiguration().getQueryOptions().setFetchSize(FETCH_SIZE);


		// Get timestamp limits as offsets, then execute query using offsets
		Row firstOffsetRow = session.execute("SELECT kafka_offset FROM stream_timestamps WHERE stream = ? AND ts >= ? ORDER BY ts ASC LIMIT 1", stream.getId(), startDate).one();
		if (firstOffsetRow == null) {
			return;
		}
		Long firstOffset = firstOffsetRow.getLong("kafka_offset");
		Long lastOffset = session.execute("SELECT kafka_offset FROM stream_timestamps WHERE stream = ? AND ts <= ? ORDER BY ts DESC LIMIT 1", stream.getId(), endDate).one().getLong("kafka_offset");

		resultSet = session.execute("SELECT payload FROM stream_events WHERE stream = ? AND kafka_offset >= ? and kafka_offset <= ? ORDER BY kafka_offset ASC", stream.getId(), firstOffset, lastOffset);
		resultSetIterator = resultSet.iterator();
	}

	@Override
	public boolean hasNext() {
		return resultSetIterator != null && resultSetIterator.hasNext();
	}

	@Override
	public MapMessage next() {
		Row row = resultSetIterator.next();

		// Async-fetch more rows if not many left
		if (resultSet.getAvailableWithoutFetching() == PREFETCH_WHEN_REMAINING && !resultSet.isFullyFetched()) {
			resultSet.fetchMoreResults(); // this is asynchronous
		}

		StreamrBinaryMessage msg = new StreamrBinaryMessage(row.getBytes("payload"));
		if (msg.getContentType() == StreamrBinaryMessage.CONTENT_TYPE_JSON) {
			return new MapMessage(new Date(msg.getTimestamp()), new Date(msg.getTimestamp()), (JSONObject) JSON.parse(msg.toString()));
		}
		else {
			throw new RuntimeException("Received payload in unknown format: "+msg.toString());
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Unsupported!");
	}

	@Override
	public void close() throws IOException {
		if (session != null) {
			session.getCluster().close();
		}
	}
}
