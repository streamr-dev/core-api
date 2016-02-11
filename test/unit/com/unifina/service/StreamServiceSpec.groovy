package com.unifina.service

import com.unifina.api.ValidationException
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.feed.mongodb.MongoStreamListener
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(ControllerUnitTestMixin) // JSON support
@TestFor(StreamService)
@Mock([Stream, Feed])
class StreamServiceSpec extends Specification {

	Feed feed

	def setup() {
		feed = new Feed(streamListenerClass: MongoStreamListener.name).save(validate: false)
	}

	void "createStream throws ValidationException input incomplete"() {

		when:
		service.createStream([feed: feed], null, null)

		then:
		thrown(ValidationException)
	}

	void "createStream results in persisted Stream"() {
		when:
		service.createStream([name: "name", feed: feed], null, null)

		then:
		Stream.count() == 1
		Stream.list().first().name == "name"
	}

	// TODO: test calls to AbstractStreamListener
}
