package com.unifina.domain

import spock.lang.Specification
import spock.lang.Unroll

class CustomStreamIDValidatorSpec extends Specification {
	@Unroll
	void "validate #value"(String value, Boolean expected) {
		def User mockUser = new User()
		def domainValidator = {domain, creator -> ((creator == mockUser) && (domain.equals("sub.my-domain.eth")))}
		def validator = new CustomStreamIDValidator(domainValidator)
		expect:
		validator.validate(value, mockUser) == expected
		where:
		value | expected
		"sandbox/a" | true
		"sandbox/abc/def" | true
		"sandbox/abc/def/file.txt" | true
		"sandbox/foo.bar/lorem.ipsum" | true
		"sandbox/foo-bar/lorem.ipsum" | true
		"sub.my-domain.eth/abc/def" | true
		"sandbox/foo-bar/lorem~ipsum" | false
		"sandbox/abc/def/" | false
		"sandbox/abc/def/file." | false
		"sandbox/foo//bar" | false
		"foobar.eth/abc/def" | false
		"foo" | false
		"sandbox" | false
		"" | false
		" " | false
	}
}
