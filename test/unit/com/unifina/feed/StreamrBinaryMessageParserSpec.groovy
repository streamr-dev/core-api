package com.unifina.feed

import com.unifina.data.StreamrBinaryMessage
import spock.lang.Specification

import java.nio.ByteBuffer

class StreamrBinaryMessageParserSpec extends Specification {

	StreamrBinaryMessage msg = new StreamrBinaryMessage("testId", 0, System.currentTimeMillis(), 100,
		StreamrBinaryMessage.CONTENT_TYPE_JSON, "{\"foo1\":\"hello\",\"foo2\":\"hello\",\"foo3\":\"hello\",\"foo4\":\"hello\",\"bar\":24.5}".getBytes("UTF-8"))
	StreamrBinaryMessageParser parser

	def setup() {
		parser = new StreamrBinaryMessageParser()
	}

	def "JSON parser preserves order of keys"() {
		List correctKeys = ["foo1", "foo2", "foo3", "foo4", "bar"]
		Map map

		when:
		map = parser.parse(msg).payload

		then:
		map.keySet().asList() == correctKeys
	}
}
