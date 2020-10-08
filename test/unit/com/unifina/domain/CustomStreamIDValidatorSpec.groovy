package com.unifina.domain

import spock.lang.Specification
import spock.lang.Unroll

class CustomStreamIDValidatorSpec extends Specification {
	@Unroll
	void "validate #value"(String value, Boolean expected) {
		expect:
		CustomStreamIDValidator.validate(value) == expected
		where:
		value | expected
		"sandbox/a" | true
		"sandbox/abc/def" | true
		"sandbox/abc/def/file.txt" | true
		"sandbox/foo.bar/lorem.ipsum" | true
		"sandbox/abc/def/" | false
		"sandbox/abc/def/file." | false
		"sandbox/foo//bar" | false
		"foobar.eth/abc/def" | false
	}
}
