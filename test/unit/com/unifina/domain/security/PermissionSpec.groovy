package com.unifina.domain.security

import com.unifina.domain.data.Stream
import com.unifina.domain.signalpath.Canvas
import spock.lang.Specification

class PermissionSpec extends Specification {
	void "validation fails if no domain class attached"() {
		expect:
		!new Permission().validate()
	}

	void "validation fails if more than 1 domain class attached"() {
		expect:
		!new Permission(canvas: new Canvas(), stream: new Stream()).validate()
	}

	void "validation succeeds if exactly 1 domain class attached"() {
		expect:
		new Permission(canvas: new Canvas()).validate()
	}
}
