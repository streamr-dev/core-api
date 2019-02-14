package com.unifina.service

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.unifina.data.StreamrBinaryMessage
import com.unifina.domain.data.Stream
import com.unifina.feed.StreamrMessage
import com.unifina.feed.redis.StreamrBinaryMessageWithKafkaMetadata
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.DisposableBean

import javax.annotation.PostConstruct
import java.nio.ByteBuffer

@CompileStatic
class CassandraService implements DisposableBean {
	static transactional = false

	GrailsApplication grailsApplication

	private static final int FETCH_SIZE = 5000;

	// Thread-safe
	private Session session

	@PostConstruct
	void init() {
		// Connects to the cluster on startup
		getSession()
	}

	/**
	 * Returns a thread-safe session connected to the Streamr Cassandra cluster.
     */
	Session getSession() {
		if (session==null) {
			Cluster.Builder builder = Cluster.builder();
			for (String host : grailsApplication.config["streamr"]["cassandra"]["hosts"]) {
				builder.addContactPoint(host);
			}
			Cluster cluster = builder.build();

			session = cluster.connect(grailsApplication.config["streamr"]["cassandra"]["keySpace"].toString());
			session.getCluster().getConfiguration().getQueryOptions().setFetchSize(FETCH_SIZE);
		}

		return session
	}

	void save(StreamrBinaryMessageWithKafkaMetadata msg) {
		Session session = getSession()
		StreamrBinaryMessage msgPayload = msg.getStreamrBinaryMessage()
		session.executeAsync("INSERT INTO stream_events (stream, stream_partition, kafka_partition, kafka_offset, previous_offset, ts, payload) values (?, ?, ?, ?, ?, ?, ?) ${msgPayload.getTTL() > 0 ? "USING TTL ${msgPayload.getTTL()}" : ""}",
				msgPayload.getStreamId(),
				msgPayload.getPartition(),
				msg.getKafkaPartition(),
				msg.getOffset(),
				msg.getPreviousOffset(),
				new Date(msgPayload.getTimestamp()),
				ByteBuffer.wrap(msgPayload.toBytes()))

		session.executeAsync("INSERT INTO stream_timestamps (stream, stream_partition, kafka_offset, ts) values (?, ?, ?, ?) ${msgPayload.getTTL() > 0 ? "USING TTL ${msgPayload.getTTL()}" : ""}",
				msgPayload.getStreamId(),
				msgPayload.getPartition(),
				msg.getOffset(),
				new Date(msgPayload.getTimestamp()))
	}

	void deleteAll(Stream stream) {
		for (int partition=0; partition<stream.partitions; partition++) {
			session.execute("DELETE FROM stream_events where stream = ? and stream_partition = ?", stream.id, partition)
			session.execute("DELETE FROM stream_timestamps where stream = ? and stream_partition = ?", stream.id, partition)
		}
	}

	void deleteRange(Stream stream, Date from, Date to) {
		for (int partition=0; partition<stream.partitions; partition++) {
			Long fromOffset = getFirstKafkaOffsetAfter(stream, partition, from);
			Long toOffset = getLastKafkaOffsetBefore(stream, partition, to);
			session.execute("DELETE FROM stream_events WHERE stream = ? AND stream_partition = ? AND kafka_offset >= ? AND kafka_offset <= ?", stream.id, partition, fromOffset, toOffset)
			session.execute("DELETE FROM stream_timestamps WHERE stream = ? AND stream_partition = ? AND ts >= ? AND ts <= ?", stream.id, partition, from, to)
		}
	}

	void deleteUpTo(Stream stream, Date to) {
		for (int partition=0; partition<stream.partitions; partition++) {
			Long toOffset = getLastKafkaOffsetBefore(stream, partition, to);
			session.execute("DELETE FROM stream_events WHERE stream = ? AND stream_partition = ? AND kafka_offset <= ?", stream.id, partition, toOffset)
			session.execute("DELETE FROM stream_timestamps WHERE stream = ? AND stream_partition = ? AND ts <= ?", stream.id, partition, to)
		}
	}

	Long getFirstKafkaOffsetAfter(Stream stream, int partition, Date date) {
		Row row = session.execute("SELECT kafka_offset FROM stream_timestamps WHERE stream = ? AND stream_partition = ? AND ts >= ? ORDER BY ts ASC LIMIT 1", stream.getId(), partition, date).one();
		if (row) {
			return row.getLong("kafka_offset");
		} else {
			return null
		}
	}

	Long getLastKafkaOffsetBefore(Stream stream, int partition, Date date) {
		Row row = session.execute("SELECT kafka_offset FROM stream_timestamps WHERE stream = ? AND stream_partition = ? AND ts <= ? ORDER BY ts DESC LIMIT 1", stream.getId(), partition, date).one()
		if (row) {
			return row.getLong("kafka_offset");
		} else {
			return null
		}
	}

	StreamrBinaryMessageWithKafkaMetadata getLatest(Stream stream, int partition) {
		ResultSet resultSet = getSession().execute("SELECT payload, kafka_partition, kafka_offset FROM stream_events WHERE stream = ? AND stream_partition = ? ORDER BY kafka_offset DESC LIMIT 1", stream.getId(), partition);
		Row row = resultSet.one();
		if (row) {
			return new StreamrBinaryMessageWithKafkaMetadata(row.getBytes("payload"), row.getInt('kafka_partition'), row.getLong('kafka_offset'), null);
		} else {
			return null
		}
	}

	StreamrMessage getLatestFromAllPartitions(Stream stream) {
		final List<StreamrMessage> messages = new ArrayList<>()
		for (int i = 0; i < stream.getPartitions(); i++) {
			final StreamrBinaryMessageWithKafkaMetadata meta = getLatest(stream, i)
			if (meta == null) {
				continue
			}
			final StreamrBinaryMessage bin = meta.getStreamrBinaryMessage()
			final StreamrMessage msg = bin.toStreamrMessage()
			messages.add(msg)
		}
		if (messages.size() < 1) {
			return null
		}
		Date now = new Date(0)
		StreamrMessage latest = null
		for (StreamrMessage m : messages) {
			if (m.getTimestamp().after(now)) {
				now = m.getTimestamp()
				latest = m
			}
		}
		return latest
	}

	StreamrBinaryMessageWithKafkaMetadata getLatestBeforeOffset(Stream stream, int partition, long offset) {
		ResultSet resultSet = getSession().execute("SELECT payload, kafka_partition, kafka_offset FROM stream_events WHERE stream = ? AND stream_partition = ? AND kafka_offset < ? ORDER BY kafka_offset DESC LIMIT 1", stream.getId(), partition, offset);
		Row row = resultSet.one();
		if (row) {
			return new StreamrBinaryMessageWithKafkaMetadata(row.getBytes("payload"), row.getInt('kafka_partition'), row.getLong('kafka_offset'), null);
		} else {
			return null
		}
	}

	void destroy() throws Exception {
		if (session) {
			session.close()
		}
	}
}
