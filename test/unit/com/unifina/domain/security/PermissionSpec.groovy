package com.unifina.domain.security

import com.unifina.domain.data.Stream
import com.unifina.domain.signalpath.Canvas
import spock.lang.Specification

class PermissionSpec extends Specification {
	void "validation fails if no domain class attached"() {
		expect:
		!new Permission(operation: Permission.Operation.CANVAS_GET).validate()
	}

	void "validation fails if more than 1 domain class attached"() {
		expect:
		!new Permission(operation: Permission.Operation.CANVAS_GET, canvas: new Canvas(), stream: new Stream()).validate()
	}

	void "validation succeeds if exactly 1 domain class attached"() {
		expect:
		new Permission(operation: Permission.Operation.CANVAS_GET, canvas: new Canvas()).validate()
	}

	void "all items in resourceFields are defined as fields"() {
		Permission p = new Permission(operation: Permission.Operation.CANVAS_GET)

		when:
		Permission.resourceFields.each {
			p[it] // throws if field doesn't exist
		}

		then:
		noExceptionThrown()
	}
}
