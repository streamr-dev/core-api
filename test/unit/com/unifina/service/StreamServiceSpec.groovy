package com.unifina.service

import com.unifina.api.ValidationException
import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.feed.AbstractStreamListener
import com.unifina.feed.DataRange
import com.unifina.feed.NoOpStreamListener
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(StreamService)
@Mock([Stream, Feed, FeedFile, SecUser, KafkaService, FeedFileService])
class StreamServiceSpec extends Specification {

	Feed feed

	def setup() {
		feed = new Feed(
			streamListenerClass: NoOpStreamListener.name
		).save(validate: false)
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

	void "createStream uses its params"() {
		setup:
		def feed = new Feed(id: 0, streamListenerClass: "com.unifina.feed.kafka.KafkaStreamListener").save(validate: false)
		when:
		def params = [
				name: "Test stream",
				description: "Test stream",
				feed: feed,
				config: [
						fields: [
								[name: "profit", type: "number"],
								[name: "keyword", type: "string"]
						]
				]
		]
		service.createStream(params, new SecUser(username: "me").save(validate: false))
		then:
		Stream.count() == 1
		def stream = Stream.findAll().get(0)
		stream.name == "Test stream"
		stream.description == "Test stream"
		stream.feed == feed
		stream.user.username == "me"
	}

	void "createStream uses Feed.KAFKA_ID as default value for feed"() {
		setup:
		def feed = new Feed(streamListenerClass: "com.unifina.feed.kafka.KafkaStreamListener")
		feed.id = Feed.KAFKA_ID
		feed.save(validate: false)
		when:
		def params = [
				name: "Test stream",
				description: "Test stream",
				config: [
						fields: [
								[name: "profit", type: "number"],
								[name: "keyword", type: "string"]
						]
				]
		]
		service.createStream(params, new SecUser(username: "me").save(validate: false))
		then:
		Stream.count() == 1
		def stream = Stream.findAll().get(0)
		stream.feed == feed
	}

	void "createStream initializes streamListener"() {
		setup:
		def service = Spy(StreamService)
		def streamListener = Mock(AbstractStreamListener)
		def params = [
				name: "Test stream",
				description: "Test stream",
				feed: feed,
				config: [
						fields: [
								[name: "profit", type: "number"],
								[name: "keyword", type: "string"]
						]
				]
		]
		when:

		service.createStream(params, new SecUser(username: "me").save(validate: false))
		then:
		1 * service.instantiateListener(_ as Stream) >> streamListener
		1 * streamListener.addToConfiguration(params.config, _ as Stream)
	}

	void "createStream throws exception if feed has no streamListenerClass"() {
		when:
		def params = [
				name: "Test stream",
				description: "Test stream",
				feed: new Feed().save(validate: false),
				config: [
						fields: [
								[name: "profit", type: "number"],
								[name: "keyword", type: "string"]
						]
				]
		]
		service.createStream(params, new SecUser(username: "me").save(validate: false))
		then:
		thrown IllegalArgumentException
	}

}
