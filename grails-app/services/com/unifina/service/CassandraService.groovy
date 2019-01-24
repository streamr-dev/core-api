package com.unifina.service

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.unifina.domain.data.Stream
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.DisposableBean
import com.streamr.client.protocol.message_layer.StreamMessage

import javax.annotation.PostConstruct
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

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

	StreamMessage getLatestStreamMessage(Stream stream, int partition) {
		ResultSet resultSet = getSession().execute("SELECT payload FROM stream_data WHERE id = ? AND partition = ? ORDER BY ts DESC, sequence_no DESC LIMIT 1", stream.getId(), partition)
		Row row = resultSet.one()
		if (row) {
			return StreamMessage.fromJson(new String(row.getBytes("payload").array(), StandardCharsets.UTF_8))
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
