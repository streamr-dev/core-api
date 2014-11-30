package com.unifina.push

import static org.junit.Assert.*
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin

import spock.lang.Specification

@TestMixin(ControllerUnitTestMixin)
class SocketIOPushChannelTests {
	
	SocketIOPushChannel io

	void setUp() {
		io = new SocketIOPushChannel("test")
		assert io.isConnected()
	}
	
	void tearDown() {
		io.destroy()
		assert !io.isConnected()
	}
	
    void testPush() {
		io.push([foo:"bar"])
	}


}
