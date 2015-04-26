package com.unifina.service

import grails.test.mixin.*
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import spock.lang.Specification

import com.unifina.domain.data.Stream

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

	void "test reading a csv file and producing feedfiles and schema"() {
		setup:
		InputStream fis = getClass().getResourceAsStream("test-upload-file.csv")
		Stream stream = new Stream(id:1)
		def feedFileService = Mock(FeedFileService)
		
		when:
		List fields = service.createFeedFilesFromCsv(fis, stream, feedFileService)
		
		then:
		3 * feedFileService.createFeedFile(stream, _, _, _, false)
		fields.size() == 4
		fields[0].name == "price"
		fields[0].type == "number"
		fields[1].name == "size"
		fields[1].type == "number"
		fields[2].name == "really"
		fields[2].type == "boolean"
		fields[3].name == "comment"
		fields[3].type == "string"

		fis.close()
	}
	
}
