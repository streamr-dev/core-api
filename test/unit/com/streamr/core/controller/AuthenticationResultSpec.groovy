package com.streamr.core.controller

import com.streamr.core.domain.Role
import com.streamr.core.domain.User
import com.streamr.core.domain.UserRole
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
	void "does not guarantee level #level when constructed with nothing"(AuthLevel level) {
		expect:
		!new AuthenticationResult(false, false).guarantees(level)

		where:
		level << [AuthLevel.USER]
	}

	void "guarantees level NONE when constructed with nothing"() {
		expect:
		new AuthenticationResult(false, false).guarantees(AuthLevel.NONE)
	}

	void "does not guarantee level NONE when authentication failed"() {
		expect:
		!new AuthenticationResult(false, true).guarantees(AuthLevel.NONE)
	}

	void "hasOneOfRoles() returns false given empty array of roles"() {
		expect:
		!new AuthenticationResult(new User()).hasOneOfRoles(new AllowRole[0])
	}

	void "hasOneOfRoles() validates that user belongs to at least one of the allow roles"() {
		User anonymousUser = new User()
		anonymousUser.save(failOnError: true, validate: false)
		User devOpsUser = new User()
		devOpsUser.save(failOnError: true, validate: false)
		Role secRole = new Role(authority: "ROLE_DEV_OPS")
		secRole.save(failOnError: true)
		new UserRole(user: devOpsUser, role: secRole).save(failOnError: true)

		expect:
		new AuthenticationResult(anonymousUser).hasOneOfRoles([AllowRole.NO_ROLE_REQUIRED] as AllowRole[])
		!new AuthenticationResult(anonymousUser).hasOneOfRoles([AllowRole.ADMIN, AllowRole.DEVOPS] as AllowRole[])
		new AuthenticationResult(devOpsUser).hasOneOfRoles([AllowRole.ADMIN, AllowRole.DEVOPS] as AllowRole[])
	}
}
