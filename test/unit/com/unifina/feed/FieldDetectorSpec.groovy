package com.unifina.feed

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.streamr.client.protocol.message_layer.StreamMessage
import spock.lang.Specification

import java.text.DateFormat

class FieldDetectorSpec extends Specification {

	StreamMessage msg

	private Gson gson = new GsonBuilder()
		.serializeNulls()
		.setDateFormat(DateFormat.LONG)
		.create()

	def setup() {
		msg = Mock(StreamMessage)
	}

	def "returns null given no message"() {
		expect:
		FieldDetector.detectFields(null) == null
	}

	def "detects 0 fields given empty message"() {
		msg.getContent() >> [:]

		expect:
		FieldDetector.detectFields(msg) == []
	}

	def "detects simple fields given flat message"() {
		msg.getContent() >> [a: 666, b: 312.0, c: "sss", d: true]

		expect:
		FieldDetector.detectFields(msg)*.toMap() == [
			[name: "a", type: "number"],
			[name: "b", type: "number"],
			[name: "c", type: "string"],
			[name: "d", type: "boolean"],
		]
	}

	def "detects maps and list fields given structured message"() {
		msg.getContent() >> [a: [1,2,3], b: [hello: "world"]]

		expect:
		FieldDetector.detectFields(msg)*.toMap() == [
			[name: "a", type: "list"],
			[name: "b", type: "map"],
		]
	}

	def "can flatten to simple fields given structured message"() {
		msg.getContent() >> [a: [1,2,3], b: [hello: "world", "beast": 666], c: true]

		expect:
		FieldDetector.detectFields(msg)*.toMap() == [
			[name: "a", type: "list"],
			[name: "b.hello", type: "string"],
			[name: "b.beast", type: "number"],
			[name: "c", type: "boolean"]
		]
	}

}
