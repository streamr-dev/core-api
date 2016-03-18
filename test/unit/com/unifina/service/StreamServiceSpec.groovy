package com.unifina.service

import com.unifina.api.ValidationException
import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.feed.DataRange
import com.unifina.feed.kafka.KafkaDataRangeProvider
import com.unifina.feed.kafka.KafkaStreamListener
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
@Mock([Stream, Feed, FeedFile, SecUser, KafkaService, FeedFileService])
class StreamServiceSpec extends Specification {

	Feed feed

	def setup() {
		feed = new Feed(streamListenerClass: KafkaStreamListener.name, dataRangeProviderClass: KafkaDataRangeProvider.name).save(validate: false)
	}

	void "createStream throws ValidationException input incomplete"() {

		when:
		service.createStream([feed: feed], null)

		then:
		thrown(ValidationException)
	}

	void "createStream results in persisted Stream"() {
		when:
		service.createStream([name: "name", feed: feed], null)

		then:
		Stream.count() == 1
		Stream.list().first().name == "name"
	}

	// TODO: test calls to AbstractStreamListener

	void "getDataRange gives correct values"() {
		DataRange dataRange
		Stream stream

		setup:
		SecUser user = new SecUser(id: 1, username: "user@user.com", password: "pwd", name:"name", enabled:true, timezone: "Europe/Helsinki")
		user.save()
		stream = service.createStream([name: "streamName", feed: feed], user)
		FeedFile startFile = new FeedFile(name:"start", feed:feed, stream: stream, day: new Date(1440000000000), beginDate: new Date(1440000000000), endDate: new Date(1440000000000))
		FeedFile endFile = new FeedFile(name:"end", feed:feed, stream: stream, day: new Date(1450000000000), beginDate: new Date(1450000000000), endDate: new Date(1450000000000))
		startFile.save()
		endFile.save()

		when: "asked for the dataRange"
		dataRange = service.getDataRange(stream)

		then: "the dates are correct"
		dataRange != null
		dataRange.beginDate == new Date(1440000000000)
		dataRange.endDate == new Date(1450000000000)
	}

	void "getDataRange gives empty values if there are no feedFiles"(){
		DataRange dataRange
		Stream stream

		setup:
		SecUser user = new SecUser(id: 1, username: "user@user.com", password: "pwd", name:"name", enabled:true, timezone: "Europe/Helsinki")
		user.save()
		stream = service.createStream([name: "streamName", feed: feed], user)

		when: "asked for the dataRange"
		dataRange = service.getDataRange(stream)

		then: "null is returned"
		dataRange == null
	}
}
