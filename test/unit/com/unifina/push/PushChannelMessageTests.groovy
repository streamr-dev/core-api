package com.unifina.push

import static org.junit.Assert.*
import grails.converters.JSON
import grails.test.mixin.*
import grails.test.mixin.support.*
import grails.test.mixin.web.ControllerUnitTestMixin

import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(ControllerUnitTestMixin) // We're not testing a controller but this inits the JSON converter
class PushChannelMessageTests {

	void setUp() {

	}
	
	void tearDown() {
	    
	}

	void testToJSON() {
		Map content = [foo:"bar"]
		PushChannelMessage message = new PushChannelMessage("channel",content)
		assert message.content == content
		assert message.channel == "channel"

		String s
		def m
		s = message.toJSON(new JSON())
		m = JSON.parse(s)

		assert m.foo == "bar"
	}
	
}
