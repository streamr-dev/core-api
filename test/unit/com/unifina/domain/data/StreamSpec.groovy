package com.unifina.domain.data

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(Stream)
class StreamSpec extends Specification {
	void "parseModuleID happy path"(String path, Integer moduleID) {
		expect:
		Stream stream = new Stream(uiChannelPath: path)
		stream.parseModuleID() == moduleID

		where:
		path | moduleID
		"/canvases/BhuD7bPlRfaQ3h99pynVMw620jSlg7T-y5jHU0MR3IgQ/modules/1493434540" | 1493434540
		"/canvases/BhuD7bPlRfaQ3h99pynVMw620jSlg7T-y5jHU0MR3IgQ/modules/765765/abcxyz" | 765765
	}

	void "parseModuleID error"() {
		setup:
		Stream stream

		when:
		stream = new Stream(uiChannelPath: "/canvases/yVGVl-FRRoSvT1iyqmGnpg2W-UVwaNS7eXTgdL1Tu-xw")
		stream.parseModuleID()
		then:
		thrown(IllegalArgumentException)

		when:
		stream = new Stream(uiChannelPath: null)
		stream.parseModuleID()
		then:
		thrown(IllegalArgumentException)

		when:
		stream = new Stream(uiChannelPath: "")
		stream.parseModuleID()
		then:
		thrown(IllegalArgumentException)
	}
}
