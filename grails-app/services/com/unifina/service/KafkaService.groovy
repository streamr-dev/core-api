package com.unifina.service

import com.unifina.data.StreamrBinaryMessage
import groovy.transform.CompileStatic
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Partitioner
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.Cluster
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.DisposableBean

class KafkaService implements DisposableBean {

	static transactional = false

	GrailsApplication grailsApplication

	private KafkaProducer<String, byte[]> producer = null
	private String dataTopic
	
	@CompileStatic
	private Properties getProperties() {
		return ((ConfigObject) grailsApplication.config["streamr"]["kafka"]).toProperties()
	}

	@CompileStatic
	private String getDataTopic() {
		if (!dataTopic) {
			dataTopic = grailsApplication.config["streamr"]["kafka"]["dataTopic"]
			if (!dataTopic)
				throw new RuntimeException("streamr.kafka.dataTopic not configured!")
		}
		return dataTopic
	}
	
	@CompileStatic
	private KafkaProducer<String, byte[]> getProducer() {
		if (producer == null) {
			producer = new KafkaProducer<String, byte[]>(getProperties());
		}
		return producer
	}

	@CompileStatic
    void sendMessage(StreamrBinaryMessage msg, String kafkaPartitionKey) {
		ProducerRecord<String, byte[]> record = new ProducerRecord<String, byte[]>(getDataTopic(), kafkaPartitionKey, msg.toBytes())
		getProducer().send(record);
    }

	void destroy() {
		if (producer) {
			producer.close()
		}
	}

	/**
	 * Use the same partitioner used to partition messages to different stream partitions.
	 * The default partitioner is not nice, because the Java and JS Kafka clients have different
	 * default partitioners.
	 */
	@CompileStatic
	public class CustomPartitioner implements Partitioner {

		@Override
		int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
			List partitions = cluster.partitionsForTopic(topic);
			int numPartitions = partitions.size();
			StreamService.partition(numPartitions, keyBytes)
		}

		@Override
		void close() {

		}

		@Override
		void configure(Map<String, ?> map) {

		}
	}
}
