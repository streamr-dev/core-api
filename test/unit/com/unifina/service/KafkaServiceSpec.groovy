package com.unifina.service

import grails.test.mixin.*
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

import com.unifina.domain.data.Stream

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(KafkaService)
@Mock([Stream])
class KafkaServiceSpec extends Specification {

    def setup() {
		defineBeans {

		}
    }

    def cleanup() {
		
    }

	void "test reading a csv file and producing feedfiles and schema"() {
		setup:
		InputStream fis = getClass().getResourceAsStream("test-upload-file.csv")
		Stream stream = new Stream(id:1)
		def feedFileService = Mock(FeedFileService)
		
		when:
		List fields = service.createFeedFilesFromCsv(fis, stream, feedFileService)
		
		then:
		5 * feedFileService.createFeedFile(stream, _, _, _, false)
		fields.size() == 2
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
