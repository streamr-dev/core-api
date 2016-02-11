package com.unifina.service

import com.unifina.feed.NoOpStreamListener
import grails.test.mixin.*
import grails.test.mixin.web.ControllerUnitTestMixin
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

	Feed feed = new Feed(streamListenerClass: NoOpStreamListener.class)

	void "createStream must not call kafkaService.createTopics() if input incomplete"() {
		
		when:
		service.createStream([:], null, null)

		then:
		0 * kafkaService.createTopics(_)
	}

	void "createUserStream results in persisted Stream"() {
		when:
		service.createStream([name: "name"], null, null)

		then:
		Stream.count() == 1
		Stream.list().first().name == "name"
	}

	void "createUserStream calls kafkaService.createTopics"() {
		Stream stream

		when:
		stream = service.createStream([name: "name", localId: "localId"], null, null)

		then:
		1 * kafkaService.createTopics(_)
	}
}
