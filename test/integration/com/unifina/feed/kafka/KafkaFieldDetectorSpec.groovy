package com.unifina.feed.kafka

import com.unifina.domain.data.Stream
import com.unifina.feed.map.MapMessage
import grails.test.spock.IntegrationSpec

class KafkaFieldDetectorSpec extends IntegrationSpec {

	def grailsApplication
	def kafkaService
	KafkaFieldDetector detector

	def setup() {
		detector = new KafkaFieldDetector(grailsApplication)
	}

	def cleanup() {

	}

	def "it should return empty object if the topic contains no messages" () {
		String topic = "KafkaFieldDetectorSpec-"+System.currentTimeMillis()
		kafkaService.createTopics([topic])
		Thread.sleep(2000)

		when:
		Stream stream = new Stream()
		stream.id = topic
		MapMessage msg = detector.fetchExampleMessage(stream)
		then:
		msg != null
		!msg.payload

		cleanup:
		kafkaService.deleteTopics([topic])
	}

	def "it should return the latest message from a topic" () {
		String topic = "KafkaFieldDetectorSpec-"+System.currentTimeMillis()
		kafkaService.createTopics([topic])
		Thread.sleep(2000)
		for (int i=0;i<10;i++) {
			kafkaService.sendMessage(topic, "", [i: i])
		}
		Thread.sleep(2000)

		when:
		Stream stream = new Stream()
		stream.id = topic
		MapMessage msg = detector.fetchExampleMessage(stream)
		then:
		msg.payload.i == 9

		cleanup:
		kafkaService.deleteTopics([topic])
	}

}
