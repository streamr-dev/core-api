package com.unifina.security

import com.unifina.domain.security.Key
import com.unifina.domain.security.Role
import com.unifina.domain.security.UserRole
import com.unifina.domain.security.User
import grails.test.mixin.Mock
import spock.lang.Specification
import spock.lang.Unroll

@Mock([User, Role, UserRole])
class AuthenticationResultSpec extends Specification {

	@Unroll
	void "guarantees #level when constructed with SecUser"(AuthLevel level) {
		expect:
		new AuthenticationResult(new User()).guarantees(level)

		where:
		level << AuthLevel.values()
	}

	@Unroll
	void "guarantees level #level when constructed with user-linked Key"(AuthLevel level) {
		expect:
		new AuthenticationResult(new Key(user: new User())).guarantees(level)

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
		!new AuthenticationResult(false, false, false).guarantees(level)

		where:
		level << [AuthLevel.KEY, AuthLevel.USER]
	}

	void "guarantees level NONE when constructed with nothing"() {
		expect:
		new AuthenticationResult(false, false, false).guarantees(AuthLevel.NONE)
	}

	void "does not guarantee level NONE when authentication failed"() {
		expect:
		!new AuthenticationResult(false, false, true).guarantees(AuthLevel.NONE)
	}

	void "hasOneOfRoles() returns false given empty array of roles"() {
		expect:
		!new AuthenticationResult(new User()).hasOneOfRoles(new AllowRole[0])
	}

	void "hasOneOfRoles() validates that user belongs to at least one of the allow roles"() {
		User user = new User()
		user.save(failOnError: true, validate: false)

		Role secRole = new Role(authority: "ROLE_DEV_OPS")
		secRole.save(failOnError: true)

		new UserRole(user: user, role: secRole).save(failOnError: true)

		expect:
		new AuthenticationResult(new Key()).hasOneOfRoles([AllowRole.NO_ROLE_REQUIRED] as AllowRole[])
		!new AuthenticationResult(new Key()).hasOneOfRoles([AllowRole.ADMIN, AllowRole.DEVOPS] as AllowRole[])
		new AuthenticationResult(user).hasOneOfRoles([AllowRole.ADMIN, AllowRole.DEVOPS] as AllowRole[])
	}
}
