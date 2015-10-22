package com.unifina.service

import grails.test.mixin.*
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import grails.validation.ValidationException
import spock.lang.Specification

import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(ControllerUnitTestMixin) // JSON support
@TestFor(StreamService)
@Mock([Stream, Feed])
class StreamServiceSpec extends Specification {

	def kafkaService
	
	def setup() {
		kafkaService = Mock(KafkaService)
		service.kafkaService = kafkaService
	}

	void "createUserStream must not call kafkaService.createTopics() if input incomplete"() {
		
		when:
		service.createUserStream([:], null)

		then:
		0 * kafkaService.createTopics(_)
	}

	void "createUserStream results in persisted Stream"() {
		when:
		service.createUserStream([name: "name", localId: "localId"], null)

		then:
		Stream.count() == 1
		Stream.list().first().name == "name"
		Stream.list().first().localId == "localId"
	}

	void "createUserStream calls kafkaService.createTopics"() {
		Stream stream

		when:
		stream = service.createUserStream([name: "name", localId: "localId"], null)

		then:
		1 * kafkaService.createTopics(_)
	}
}
