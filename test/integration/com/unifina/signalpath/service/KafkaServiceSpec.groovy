package com.unifina.signalpath.service

import com.unifina.domain.data.Stream
import com.unifina.service.FeedFileService
import com.unifina.utils.CSVImporter
import grails.test.spock.IntegrationSpec
import spock.util.concurrent.PollingConditions

import java.nio.file.Paths

class KafkaServiceSpec extends IntegrationSpec {

	def kafkaService

	void "test creating and deleting a Kafka topic"() {
		def conditions = new PollingConditions(delay: 1)

		setup:
		String topic = "KafkaServiceSpec_"+new Date().time

		when:
		kafkaService.createTopics([topic])

		then:
		conditions.within(10) {
			kafkaService.topicExists(topic)
		}

		when:
		kafkaService.deleteTopics([topic])

		then:
		conditions.within(10) {
			!kafkaService.topicExists(topic)
		}
	}

	void "test producing Kafka feedfiles from csv"() {
		setup:
		File file = Paths.get(getClass().getResource("test-upload-file.csv").toURI()).toFile()
		CSVImporter csv = new CSVImporter(file)
		Stream stream = new Stream(id:1)
		def feedFileService = Mock(FeedFileService)

		when:
		kafkaService.createFeedFilesFromCsv(csv, stream, feedFileService)

		then:
		3 * feedFileService.createFeedFile(stream, _, _, _, false)
	}
}
