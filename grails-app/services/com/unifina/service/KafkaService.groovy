package com.unifina.service

import grails.converters.JSON
import groovy.transform.CompileStatic
import kafka.javaapi.producer.Producer
import kafka.producer.KeyedMessage
import kafka.producer.ProducerConfig

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.unifina.kafkaclient.UnifinaKafkaMessage;
import com.unifina.kafkaclient.UnifinaKafkaProducer

@CompileStatic
class KafkaService {

	UnifinaKafkaProducer producer = null
	GrailsApplication grailsApplication
	
    void sendMessage(String channelId, Object key, String message, boolean isJson=true) {
		if (producer == null) {
			Properties props = ((ConfigObject)grailsApplication.config["unifina"]["kafka"]).toProperties()
			ProducerConfig producerConfig = new ProducerConfig(props)
			producer = new UnifinaKafkaProducer(props)
		}

		if (isJson)
			producer.sendJSON(channelId, key.toString(), System.currentTimeMillis(), message)
		else producer.sendString(channelId, key.toString(), System.currentTimeMillis(), message)
    }
	
	void sendMessage(String channelId, Object key, Map message) {
		String str = (message as JSON).toString();
		sendMessage(channelId, key, str, true);
	}
}
