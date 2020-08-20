package com.unifina.domain

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(StreamID)
class StreamIDSpec extends Specification {
	final String validPath = "valid.eth/path"
	final String invalidPath = "invalid-eth/path"

	void "test validate valid case"() {
		when:
		StreamID name = new StreamID(validPath)
		then:
		name.validate() == true
	}

	void "test validate valid max legth"() {
		when:
		StreamID name = new StreamID("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
		then:
		name.validate() == true
	}

	void "test validate invalid max legth"() {
		when:
		StreamID name = new StreamID("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxZ")
		then:
		name.validate() == false
	}

	void "test validate invalid case"() {
		when:
		StreamID name = new StreamID(invalidPath)
		then:
		name.validate() == false
	}

	void "test toString"() {
		when:
		StreamID name = new StreamID(validPath)
		then:
		name.toString() == validPath
	}

	void "test toString without path"() {
		final String domain = "valid.eth"
		when:
		StreamID name = new StreamID(domain)
		then:
		name.toString() == domain
	}
}
