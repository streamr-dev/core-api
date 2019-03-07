package com.unifina.domain.data

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(Stream)
class StreamSpec extends Specification {
	void "generate IDs for config fields"() {
		setup:
		Stream s = new Stream(
			config: '{"fields":[{"name":"veh","type":"string"},{"name":"lat","type":"number"},{"name":"long","type":"number"}]}',
			feed: new Feed(),
		)

		when:
		def conf = s.toMap().config

		then:
		conf == [fields: [
			[name: "veh", type: "string", id: "vehstring".hashCode()],
			[name: "lat", type: "number", id: "latnumber".hashCode()],
			[name: "long", type: "number", id: "longnumber".hashCode()],
		]]
	}
}
