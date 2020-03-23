package com.unifina.service


import com.datastax.driver.core.Cluster
import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.streamr.client.protocol.message_layer.StreamMessage
import com.unifina.domain.data.Stream
import com.unifina.feed.DataRange
import com.unifina.feed.util.StreamMessageComparator
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.DisposableBean

import javax.annotation.PostConstruct
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

@CompileStatic
class CassandraService implements DisposableBean {
	static transactional = false

	GrailsApplication grailsApplication

	private static final int FETCH_SIZE = 5000;

	static final long ONE_WEEK_IN_MS = 7L * 24L * 60L * 60L * 1000L

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
			Cluster.Builder builder = Cluster.builder()
			for (String host : grailsApplication.config["streamr"]["cassandra"]["hosts"]) {
				builder.addContactPoint(host)
			}
			if (grailsApplication.config["streamr"]["cassandra"]["username"] && grailsApplication.config["streamr"]["cassandra"]["password"]) {
				builder.withCredentials(
					grailsApplication.config["streamr"]["cassandra"]["username"].toString(),
					grailsApplication.config["streamr"]["cassandra"]["password"].toString()
				)
			}
			Cluster cluster = builder.build()

			session = cluster.connect(grailsApplication.config["streamr"]["cassandra"]["keySpace"].toString());
			session.getCluster().getConfiguration().getQueryOptions().setFetchSize(FETCH_SIZE);
		}

		return session
	}

	void save(StreamMessage msg) {
		Session session = getSession()
		session.executeAsync("INSERT INTO stream_data (id, partition, ts, sequence_no, publisher_id, payload) values (?, ?, ?, ?, ?, ?)",
			msg.getStreamId(),
			msg.getStreamPartition(),
			new Date(msg.getTimestamp()),
			msg.getSequenceNumber(),
			msg.getPublisherId(),
			ByteBuffer.wrap(msg.toBytes()))
	}

	void deleteAll(Stream stream) {
		for (int partition=0; partition<stream.partitions; partition++) {
			session.execute("DELETE FROM stream_data where id = ? and partition = ?", stream.id, partition)
		}
	}

	void deleteRange(Stream stream, Date from, Date to) {
		for (int partition=0; partition<stream.partitions; partition++) {
			session.execute("DELETE FROM stream_data WHERE id = ? AND partition = ? AND ts >= ? AND ts <= ?", stream.id, partition, from, to)
		}
	}

	void deleteUpTo(Stream stream, Date to) {
		for (int partition=0; partition<stream.partitions; partition++) {
			session.execute("DELETE FROM stream_data WHERE id = ? AND partition = ? AND ts <= ?", stream.id, partition, to)
		}
	}

	private StreamMessage getExtremeStreamMessage(Stream stream, int partition, boolean latest) {
		ResultSet resultSet
		if (latest) {
			// TODO: ts >= ? condition added to prevent timeouts of cassandra queries. A more efficient approach to finding
			// the latest message is needed. (CORE-1724)
			resultSet = getSession()
				.execute("SELECT payload FROM stream_data WHERE id = ? AND partition = ? AND ts >= ? ORDER BY ts DESC, sequence_no DESC LIMIT 1",
					stream.getId(), partition, System.currentTimeMillis() - ONE_WEEK_IN_MS)
		} else {
			resultSet = getSession().execute("SELECT payload FROM stream_data WHERE id = ? AND partition = ? ORDER BY ts ASC, sequence_no ASC LIMIT 1", stream.getId(), partition)
		}
		Row row = resultSet.one()
		if (row) {
			return StreamMessage.fromJson(new String(row.getBytes("payload").array(), StandardCharsets.UTF_8))
		} else {
			return null
		}
	}

	private StreamMessage getExtremeFromAllPartitions(Stream stream, boolean latest) {
		final List<StreamMessage> messages = new ArrayList<>()
		for (int i = 0; i < stream.getPartitions(); i++) {
			final StreamMessage msg = getExtremeStreamMessage(stream, i, latest)
			if (msg != null) {
				messages.add(msg)
			}
		}
		if (messages.size() < 1) {
			return null
		}

		StreamMessage extreme = null
		Comparator<StreamMessage> streamMessageComparator = new StreamMessageComparator()
		for (StreamMessage m : messages) {
			if (extreme == null || streamMessageComparator.compare(m, extreme) == (latest ? 1 : -1)) {
				extreme = m
			}
		}
		return extreme
	}

	StreamMessage getLatestStreamMessage(Stream stream, int partition) {
		return getExtremeStreamMessage(stream, partition, true)
	}

	StreamMessage getEarliestStreamMessage(Stream stream, int partition) {
		return getExtremeStreamMessage(stream, partition, false)
	}

	StreamMessage getLatestFromAllPartitions(Stream stream) {
		return getExtremeFromAllPartitions(stream, true)
	}

	StreamMessage getEarliestFromAllPartitions(Stream stream) {
		return getExtremeFromAllPartitions(stream, false)
	}

	DataRange getDataRange(Stream stream) {
		StreamMessage earliest = getEarliestFromAllPartitions(stream)
		StreamMessage latest = getLatestFromAllPartitions(stream)
		if (!earliest || !latest) {
			return null
		} else {
			return new DataRange(earliest.getTimestampAsDate(), latest.getTimestampAsDate())
		}
	}

	void destroy() throws Exception {
		if (session) {
			session.close()
		}
	}
}
