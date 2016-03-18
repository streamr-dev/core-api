package com.unifina.feed

import com.unifina.feed.mongodb.MongoStreamListener
import spock.lang.Specification

class MongoStreamListenerSpec extends Specification {
	void "addToConfiguration inserts empty mongodb entry"() {
		AbstractStreamListener listener = new MongoStreamListener(null)
		def map = [a: "a"]

		when:
		listener.addToConfiguration(map, null)

		then:
		map == [a: "a", mongodb: [:]]
	}
}
