package com.unifina.signalpath

import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.service.FeedService
import com.unifina.utils.Globals
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([Feed, Stream])
class StreamParameterSpec extends Specification {

	AbstractSignalPathModule ownerModule = Stub(AbstractSignalPathModule)
	StreamParameter streamParameter = new StreamParameter(ownerModule, "name")

	def setup() {

		Feed feed = new Feed()
		feed.id = 6152
		feed.save(failOnError: true, validate: false)

		Stream stream = new Stream()
		stream.id = "stream-id"
		stream.name = "stream-name"
		stream.feed = feed
		stream.save(failOnError: true, validate: false)

		ownerModule.globals = Stub(Globals) { getBean(FeedService) >> new FeedService() }
	}

	def "has typeName of 'Stream'"() {
		expect:
		streamParameter.typeName == "Stream"
	}

	def "acceptedTypes are String and Stream"() {
		expect:
		streamParameter.acceptedTypes as Set == ["String", "Stream"] as Set
	}

	def "cannot toggle driving input"() {
		expect:
		!streamParameter.canToggleDrivingInput
	}

	def "has null value after initialization"() {
		expect:
		streamParameter.getValue() == null
	}

	def "getConfiguration() has expected content after initialization"() {
		expect:
		streamParameter.getConfiguration().subMap("value", "streamName", "feed", "checkModuleId", "feedFilter") == [
		    checkModuleId: null,
			feed: null,
			feedFilter: null,
			streamName: null,
			value: null
		]
	}

	def "receive() fetches stream"() {
		setup:
		Stream stream = new Stream()
		stream.id = "stream-id"
		stream.save(failOnError: true, validate: false)

		ownerModule.globals = Stub(Globals) { getBean(FeedService) >> new FeedService() }

		when:
		streamParameter.receive("stream-id")

		then:
		streamParameter.getValue().id == "stream-id"
	}

	def "parseValue() fetches stream given valid id"() {
		when:
		def result = streamParameter.parseValue("stream-id")

		then:
		result != null
	}

	def "parseValue() returns null given null"() {
		when:
		def result = streamParameter.parseValue(null)

		then:
		result == null
	}

	def "parseValue() returns null given \"null\""() {
		when:
		def result = streamParameter.parseValue("null")

		then:
		result == null
	}

	def "getConfiguration() has expected content after receiving stream id"() {
		when:
		streamParameter.receive("stream-id")

		then:
		streamParameter.getConfiguration().subMap("value", "streamName", "feed", "checkModuleId", "feedFilter") == [
			checkModuleId: null,
			feed: 6152,
			feedFilter: null,
			streamName: "stream-name",
			value: "stream-id"
		]
	}
}
