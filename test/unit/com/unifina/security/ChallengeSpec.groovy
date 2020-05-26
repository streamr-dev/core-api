package com.unifina.security


import spock.lang.Specification

class ChallengeSpec extends Specification {

	void "creating a lot of challenges doesn't crash the JVM (CORE-1937) (2)"() {
		expect:
		for (int i=0; i<2000; i++) {
			new Challenge("foo", 30, 10)
		}
	}

}
