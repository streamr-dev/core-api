package com.unifina.feed

import com.unifina.domain.data.Stream
import com.unifina.feed.json.JSONStreamrMessage
import spock.lang.Specification

class FieldDetectorSpec extends Specification {

	Stream stream
	FieldDetector detector
	Map mapToReturn

	def setup() {
		stream = new Stream()
		detector = new FieldDetector(null) {
			@Override
			protected AbstractStreamrMessage fetchExampleMessage(Stream stream) {
				return new JSONStreamrMessage("streamId", 0, new Date(), new Date(), (Map) mapToReturn)
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

}
