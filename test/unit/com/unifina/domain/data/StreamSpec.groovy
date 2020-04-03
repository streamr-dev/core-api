package com.unifina.domain.data

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(Stream)
class StreamSpec extends Specification {
	void "parseModuleID"(String path, Integer moduleID) {
		expect:
		Stream stream = new Stream(uiChannelPath: path)
		stream.parseModuleID() == moduleID

		where:
		path | moduleID
		"/canvases/yVGVl-FRRoSvT1iyqmGnpg2W-UVwaNS7eXTgdL1Tu-xw" | 0
		"/canvases/BhuD7bPlRfaQ3h99pynVMw620jSlg7T-y5jHU0MR3IgQ/modules/1493434540" | 1493434540
		null | 0
		"" | 0
	}
}
