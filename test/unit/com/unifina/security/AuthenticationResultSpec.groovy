package com.unifina.security

import com.unifina.domain.security.Key
import com.unifina.domain.security.SecRole
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SecUserSecRole
import grails.test.mixin.Mock
import spock.lang.Specification
import spock.lang.Unroll

@Mock([SecUser, SecRole, SecUserSecRole])
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

	void "hasOneOfRoles() returns false given empty array of roles"() {
		expect:
		!new AuthenticationResult(new SecUser()).hasOneOfRoles(new AllowRole[0])
	}

	void "hasOneOfRoles() validates that user belongs to at least one of the allow roles"() {
		SecUser user = new SecUser()
		user.save(failOnError: true, validate: false)

		SecRole secRole = new SecRole(authority: "ROLE_DEV_OPS")
		secRole.save(failOnError: true)

		new SecUserSecRole(secUser: user, secRole: secRole).save(failOnError: true)

		expect:
		new AuthenticationResult(new Key()).hasOneOfRoles([AllowRole.NO_ROLE_REQUIRED] as AllowRole[])
		!new AuthenticationResult(new Key()).hasOneOfRoles([AllowRole.ADMIN, AllowRole.DEVOPS] as AllowRole[])
		new AuthenticationResult(user).hasOneOfRoles([AllowRole.ADMIN, AllowRole.DEVOPS] as AllowRole[])
	}
}
