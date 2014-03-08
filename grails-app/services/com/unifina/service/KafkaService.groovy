package com.unifina.service

import grails.converters.JSON
import groovy.transform.CompileStatic
import kafka.javaapi.producer.Producer
import kafka.producer.KeyedMessage
import kafka.producer.ProducerConfig

import org.codehaus.groovy.grails.commons.GrailsApplication

@CompileStatic
class KafkaService {

	Producer producer = null
	GrailsApplication grailsApplication
	
    void sendMessage(String channelId, Object key, String message) {
		if (producer == null) {
			kafka.serializer.DefaultEncoder
			Properties props = ((ConfigObject)grailsApplication.config["unifina"]["kafka"]).toProperties()
			ProducerConfig producerConfig = new ProducerConfig(props)
			producer = new Producer<String,String>(producerConfig)
		}

		KeyedMessage data = new KeyedMessage(channelId, key, message);
		producer.send(data);
    }
	
	void sendMessage(String channelId, Object key, Map message) {
		String str = (message as JSON).toString();
		sendMessage(channelId, key, str);
	}
}
