package com.unifina.service

import grails.test.mixin.*
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.web.ControllerUnitTestMixin

import java.nio.file.Paths

import spock.lang.Specification

import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.utils.CSVImporter

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(ControllerUnitTestMixin) // Initializes the JSON converter, can be used even though this test is for a service
@TestFor(KafkaService)
@Mock([Stream])
class KafkaServiceSpec extends Specification {

    def setup() {
		defineBeans {

		}
    }

    def cleanup() {
		java.util.LinkedHashMap.metaClass.asType = null
    }
	
	void "test creating and deleting a Kafka topic"() {
		setup:
		String topic = "KafkaServiceSpec_"+new Date().time
		
		when:
		service.createTopics([topic])
		then:
		service.topicExists(topic)
		
		when:
		service.deleteTopics([topic])
		Thread.sleep(2000)
		then:
		!service.topicExists(topic)
	}

	void "test producing Kafka feedfiles from csv"() {
		setup:
		File file = Paths.get(getClass().getResource("test-upload-file.csv").toURI()).toFile()
		CSVImporter csv = new CSVImporter(file)
		Stream stream = new Stream(id:1)
		def feedFileService = Mock(FeedFileService)
		
		when:
		List<FeedFile> feedFiles = service.createFeedFilesFromCsv(csv, stream, feedFileService)
		
		then:
		3 * feedFileService.createFeedFile(stream, _, _, _, false)
	}
	
}
