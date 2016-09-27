package com.unifina.service

import com.unifina.data.StreamrBinaryMessage
import com.unifina.domain.data.Stream
import com.unifina.kafkaclient.KafkaOffsetUtil
import grails.converters.JSON
import groovy.transform.CompileStatic
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.utils.Utils
import org.codehaus.groovy.grails.commons.GrailsApplication

import javax.annotation.Nullable
import java.nio.charset.Charset
import java.util.concurrent.ThreadLocalRandom

class KafkaService {

	static transactional = false

	GrailsApplication grailsApplication

	private KafkaProducer<String, byte[]> producer = null
	private String dataTopic

	private static final Charset utf8 = Charset.forName("UTF-8")
	
	@CompileStatic
	private Properties getProperties() {
		return ((ConfigObject)grailsApplication.config["streamr"]["kafka"]).toProperties()
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
	int partition(String partitionKey, int numOfPartitions) {
		if (numOfPartitions == 1) {
			// Fast common case
			return 0
		} else if (partitionKey) {
			// Borrow Kafka partitioning algorithm
			return Utils.abs(Utils.murmur2(partitionKey.getBytes(utf8))) % numOfPartitions;
		} else {
			// Fallback to random partition if no key
			return ThreadLocalRandom.current().nextInt(numOfPartitions);
		}
	}

	@CompileStatic
    void sendMessage(Stream stream, @Nullable String partitionKey=null, byte[] content, byte contentType, int ttl=0) {
		int streamPartition = partition(partitionKey, stream.getPartitions())
		StreamrBinaryMessage msg = new StreamrBinaryMessage(stream.id, streamPartition, System.currentTimeMillis(), ttl, contentType, content)
		String kafkaPartitionKey = "${stream.id}-$streamPartition"
		ProducerRecord<String, byte[]> record = new ProducerRecord<String, byte[]>(getDataTopic(), kafkaPartitionKey, msg.toBytes())
		getProducer().send(record);
    }
	
	@CompileStatic
	void sendMessage(Stream stream, @Nullable String partitionKey=null, Map message, int ttl=0) {
		String str = (message as JSON).toString();
		sendMessage(stream, partitionKey, str.getBytes(utf8), StreamrBinaryMessage.CONTENT_TYPE_JSON, ttl);
	}

	@CompileStatic
	@Deprecated
	KafkaOffsetUtil getOffsetUtil() {
		return new KafkaOffsetUtil(getProperties())
	}
}
