package com.unifina.service

import com.unifina.data.StreamrBinaryMessage
import grails.util.Holders
import groovy.transform.CompileStatic
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
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

}
