package com.unifina.signalpath

import com.unifina.BeanMockingSpecification
import com.unifina.domain.data.Stream
import com.unifina.service.StreamService
import com.unifina.utils.Globals
import grails.test.mixin.Mock

@Mock([Stream])
class StreamParameterSpec extends BeanMockingSpecification {

	AbstractSignalPathModule ownerModule
	StreamParameter streamParameter
	StreamService streamService
	Stream stream

	def setup() {
		stream = new Stream()
		stream.id = "stream-id"
		stream.name = "stream-name"
		stream.save(failOnError: true, validate: false)

		streamService = mockBean(StreamService, Mock(StreamService))
		streamService.getStream(stream.id) >> stream

		ownerModule = Mock(AbstractSignalPathModule)
		streamParameter = new StreamParameter(ownerModule, "name")
		Globals globals = Mock(Globals)
		ownerModule.getGlobals() >> globals
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
		streamParameter.getConfiguration().subMap(["value"]) == [
			value: null
		]
	}

	def "receive() fetches stream"() {
		when:
		streamParameter.receive(stream.id)

		then:
		streamParameter.getValue().id == stream.id
	}

	def "parseValue() fetches stream given valid id"() {
		when:
		Stream result = streamParameter.parseValue(stream.id)

		then:
		result == stream
	}

	def "parseValue() returns null given null"() {
		expect:
		streamParameter.parseValue(null) == null
	}

	def "parseValue() returns null given \"null\""() {
		expect:
		streamParameter.parseValue("null") == null
	}

	def "getConfiguration() has expected content after receiving stream id"() {
		when:
		streamParameter.receive(stream.id)

		then:
		streamParameter.getConfiguration().subMap("streamName", "value") == [
			streamName: stream.name,
			value: stream.id
		]
	}

	def "can be configured with null value"() {
		when:
		streamParameter.setConfiguration([value: null])

		then:
		streamParameter.getValue() == null
	}
}
