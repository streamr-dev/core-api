package com.unifina.feed

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.streamr.client.protocol.message_layer.StreamMessage
import com.streamr.client.protocol.message_layer.StreamMessageV31
import com.unifina.domain.data.Stream
import spock.lang.Specification

import java.text.DateFormat

class FieldDetectorSpec extends Specification {

	Stream stream
	FieldDetector detector
	Map mapToReturn

	private Gson gson = new GsonBuilder()
		.serializeNulls()
		.setDateFormat(DateFormat.LONG)
		.create()

	def setup() {
		stream = new Stream()
		stream.id = "stream-id"
		detector = new FieldDetector() {
			@Override
			protected StreamMessage fetchExampleMessage(Stream stream) {
				return new StreamMessageV31(stream.id, 0, System.currentTimeMillis(), 0L, "", "", (Long) null, 0L,
					StreamMessage.ContentType.CONTENT_TYPE_JSON, StreamMessage.EncryptionType.NONE, gson.toJson(mapToReturn),
					StreamMessage.SignatureType.SIGNATURE_TYPE_NONE, null)
			}
		}
	}

	def "throws NullPointerException given no message"() {
		mapToReturn = null

		when:
		detector.detectFields(stream)

		then:
		thrown(NullPointerException)
	}

	def "detects 0 fields given empty message"() {
		mapToReturn = [:]

		when:
		def result = detector.detectFields(stream)

		then:
		result == []
	}

	def "detects simple fields given flat message"() {
		mapToReturn = [a: 666, b: 312.0, c: "sss", d: true]

		when:
		def result = detector.detectFields(stream)

		then:
		result == [
			[name: "a", type: "number"],
			[name: "b", type: "number"],
			[name: "c", type: "string"],
			[name: "d", type: "boolean"],
		]
	}

	def "detects maps and list fields given structured message"() {
		mapToReturn = [a: [1,2,3], b: [hello: "world"]]

		when:
		def result = detector.detectFields(stream)

		then:
		result == [
			[name: "a", type: "list"],
			[name: "b", type: "map"],
		]
	}

	def "can flatten to simple fields given structured message"() {
		mapToReturn = [a: [1,2,3], b: [hello: "world", "beast": 666], c: true]

		when:
		detector.flattenMap = true
		def result = detector.detectFields(stream)

		then:
		result == [
			[name: "a", type: "list"],
			[name: "b.hello", type: "string"],
			[name: "b.beast", type: "number"],
			[name: "c", type: "boolean"]
		]
	}

}
