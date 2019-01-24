package com.unifina.feed.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.streamr.client.protocol.message_layer.StreamMessage;
import com.unifina.domain.data.Stream;
import com.unifina.feed.FieldDetector;
import com.unifina.feed.StreamrMessageParser;
import com.unifina.feed.map.MapMessage;
import com.unifina.service.CassandraService;
import grails.util.Holders;

/**
 * Creates a new Cassandra Session that connects to the Cluster defined in the Stream, and selects the latest message.
 * The connection is closed in the end.
 */
public class CassandraFieldDetector extends FieldDetector {

	@Override
	protected MapMessage fetchExampleMessage(Stream stream) {
		try {
			Session session = getSession();
			Row latestRow = null;
			// TODO: When we get Java 8 this would be faster using async queries and CompletableFuture.allOf to wait for Futures to complete
			for (int partition=0; partition<stream.getPartitions(); partition++) {
				ResultSet resultSet = session.execute("SELECT payload FROM stream_data WHERE id = ? AND partition = ? ORDER BY ts DESC, sequence_no DESC LIMIT 1", stream.getId(), partition);
				Row row = resultSet.one();
				if (row != null && (latestRow == null || latestRow.getTimestamp("ts").before(row.getTimestamp("ts")))) {
					latestRow = row;
				}
			}
			if (latestRow != null) {
				StreamrMessageParser parser = new StreamrMessageParser();
				return parser.parse(StreamMessage.fromBytes(latestRow.getBytes("payload").array()));
			} else {
				return null;
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Session getSession() {
		return Holders.getApplicationContext().getBean(CassandraService.class).getSession();
	}
}
