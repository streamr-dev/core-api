package com.unifina.service

import com.unifina.data.StreamrBinaryMessage
import com.unifina.domain.data.Stream
import com.unifina.kafkaclient.KafkaOffsetUtil
import grails.converters.JSON
import groovy.transform.CompileStatic
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.codehaus.groovy.grails.commons.GrailsApplication

import java.nio.charset.Charset

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
	KafkaProducer<String, byte[]> getProducer() {
		if (producer == null) {
			producer = new KafkaProducer<String, byte[]>(getProperties());
		}
		return producer
	}

	@CompileStatic
    void sendMessage(String streamId, byte[] content, byte contentType, int ttl=0) {
		StreamrBinaryMessage msg = new StreamrBinaryMessage(streamId, System.currentTimeMillis(), contentType, content, ttl)
		ProducerRecord<String, byte[]> record = new ProducerRecord<String, byte[]>(getDataTopic(), streamId, msg.toBytes())
		getProducer().send(record);
    }
	
	@CompileStatic
	void sendMessage(String streamId, Map message, int ttl=0) {
		String str = (message as JSON).toString();
		sendMessage(streamId, str.getBytes(utf8), StreamrBinaryMessage.CONTENT_TYPE_JSON, ttl);
	}
	
	@CompileStatic
	void sendMessage(Stream stream, Map message, int ttl=0) {
		String str = (message as JSON).toString();
		sendMessage(stream.getId(), str.getBytes(utf8), StreamrBinaryMessage.CONTENT_TYPE_JSON, ttl);
	}

	@CompileStatic
	@Deprecated
	KafkaOffsetUtil getOffsetUtil() {
		return new KafkaOffsetUtil(getProperties())
	}
}
