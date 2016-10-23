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

	void "partition() produces the expected partitioning"() {
		List<String> keys = (0..99).collect { "key-$it" }
		// Results must be the same as those produced by streamr-http-api/lib/partitioner.js
		List correctResults = [5, 6, 3, 9, 3, 0, 2, 8, 2, 6, 9, 5, 5, 8, 5, 0, 0, 7, 2, 8, 5, 6, 8, 1, 7, 9, 2, 1, 8, 5, 6, 4, 3, 3, 1, 7, 1, 5, 2, 8, 3, 3, 8, 6, 8, 7, 4, 8, 2, 3, 5, 2, 8, 8, 8, 9, 8, 2, 7, 7, 0, 8, 8, 5, 9, 9, 9, 7, 2, 7, 0, 4, 4, 6, 4, 8, 5, 5, 0, 8, 2, 5, 1, 8, 6, 8, 8, 1, 2, 0, 7, 3, 2, 2, 5, 7, 9, 6, 4, 7]
		Stream stream = new Stream()
		stream.partitions = 10

		when:
		List results = keys.collect {service.partition(stream, it)}

		then:
		results == correctResults
	}

}
