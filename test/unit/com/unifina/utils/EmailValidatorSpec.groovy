package com.unifina.utils

import com.unifina.domain.security.SecUserSecRole
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock(SecUserSecRole)
class EmailValidatorSpec extends Specification {
	void "validates emails correctly"() {
		expect:
		result == EmailValidator.validate(email)

		where:
		result | email
		true   | "tester1@streamr.com"
		true   | "tester_1@streamr.com"
		true   | "tester.1@streamr.com"
		true   | "huuuuuuuuugeeeeee.loooooooooooong.eeeeeemmmmmaiaaaaaaail.aaaaaaaaaaaaaaaaaadressss@streamrrrrrrrrrrrrrrrrrrrrrrrrr.com"
		true   | "weird@tld.hocuspocus"
		true   | "google-style+addition@streamr.com"
		false  | "@streamr.com"
		false  | "test@streamr."
		false  | "test@streamr"
		false  | "test"
		false  | "test.com"
	}
}
