package com.unifina.domain

import grails.test.mixin.Mock
import spock.lang.Specification

@Mock(UserRole)
class EmailValidatorSpec extends Specification {
	void "validates emails correctly"() {
		expect:
		result == EmailValidator.validate(email)

		where:
		result | email
		true   | "tester1@streamr.network"
		true   | "tester_1@streamr.network"
		true   | "tester.1@streamr.network"
		true   | "huuuuuuuuugeeeeee.loooooooooooong.eeeeeemmmmmaiaaaaaaail.aaaaaaaaaaaaaaaaaadressss@streamrrrrrrrrrrrrrrrrrrrrrrrrr.com"
		true   | "weird@tld.hocuspocus"
		true   | "google-style+addition@streamr.network"
		false  | "@streamr.network"
		false  | "test@streamr."
		false  | "test@streamr"
		false  | "test"
		false  | "test.com"
	}
}
