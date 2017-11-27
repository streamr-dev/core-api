package com.unifina.security

import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import spock.lang.Specification
import spock.lang.Unroll

class AuthenticationResultSpec extends Specification {

	@Unroll
	void "guarantees #level when constructed with SecUser"(AuthLevel level) {
		expect:
		new AuthenticationResult(new SecUser()).guarantees(level)

		where:
		level << AuthLevel.values()
	}

	@Unroll
	void "guarantees level #level when constructed with user-linked Key"(AuthLevel level) {
		expect:
		new AuthenticationResult(new Key(user: new SecUser())).guarantees(level)

		where:
		level << AuthLevel.values()
	}

	@Unroll
	void "guarantees level #level when constructed with anonymous Key"(AuthLevel level) {
		expect:
		new AuthenticationResult(new Key()).guarantees(level)

		where:
		level << [AuthLevel.NONE, AuthLevel.KEY]
	}

	void "does not guarantee level USER when constructed with anonymous Key"() {
		expect:
		!new AuthenticationResult(new Key()).guarantees(AuthLevel.USER)
	}

	@Unroll
	void "does not guarantee level #level when constructed with nothing"(AuthLevel level) {
		expect:
		!new AuthenticationResult(false, false).guarantees(level)

		where:
		level << [AuthLevel.KEY, AuthLevel.USER]
	}

	void "guarantees level NONE when constructed with nothing"() {
		expect:
		new AuthenticationResult(false, false).guarantees(AuthLevel.NONE)
	}
}
