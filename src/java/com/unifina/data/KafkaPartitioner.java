package com.unifina.data;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.List;
import java.util.Map;

/**
 * Implements a Kafka Partitioner using the same partitioning algorithm used to partition messages
 * to different Stream partitions.
 *
 * The default Kafka partitioner is not nice, because the Java and JS Kafka clients have different
 * default partitioners.
 */
public class KafkaPartitioner extends StreamPartitioner implements Partitioner {
	@Override
	public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
		List partitions = cluster.partitionsForTopic(topic);
		int numPartitions = partitions.size();
		return partition(numPartitions, keyBytes);
	}

	@Override
	public void close() {

	}

	@Override
	public void configure(Map<String, ?> map) {

	}
}
