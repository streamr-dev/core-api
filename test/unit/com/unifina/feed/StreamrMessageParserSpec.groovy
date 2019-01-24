package com.unifina.feed

import com.streamr.client.protocol.message_layer.StreamMessage
import com.streamr.client.protocol.message_layer.StreamMessageV30
import spock.lang.Specification

class StreamrMessageParserSpec extends Specification {

	StreamMessage msg = new StreamMessageV30("testId", 0, System.currentTimeMillis(), 0, "publisherId", null, null,
		StreamMessage.ContentType.CONTENT_TYPE_JSON,
		"{\"foo1\":\"hello\",\"foo2\":\"hello\",\"foo3\":\"hello\",\"foo4\":\"hello\",\"bar\":24.5}",
	StreamMessage.SignatureType.SIGNATURE_TYPE_ETH, "signature")
	StreamrMessageParser parser

	def setup() {
		parser = new StreamrMessageParser()
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
