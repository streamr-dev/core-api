package com.unifina.security


import com.unifina.domain.security.Role
import com.unifina.domain.security.User
import com.unifina.domain.security.UserRole
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock(UserRole)
class AllowRoleSpec extends Specification {

	void "hasUserRole(null) is true when this == NO_ROLE_REQUIRED"() {
		expect:
		AllowRole.NO_ROLE_REQUIRED.hasUserRole(null)
	}

	void "hasUserRole(user w/o roles) is true when this == NO_ROLE_REQUIRED"() {
		expect:
		AllowRole.NO_ROLE_REQUIRED.hasUserRole(new User())
	}

	void "hasUserRole(null) is false when this == DEVOPS"() {
		expect:
		!AllowRole.DEVOPS.hasUserRole(null)
	}


	void "hasUserRole(user w/o roles) is false when this == DEVOPS"() {
		expect:
		!AllowRole.DEVOPS.hasUserRole(new User())
	}

	void "hasUserRole(user with DEVOPS role) is true when this == DEVOPS"() {
		User user = new User()
		user.save(failOnError: true, validate: false)

		Role secRole = new Role(authority: "ROLE_DEV_OPS")
		secRole.save(failOnError: true)

		new UserRole(user: user, role: secRole).save(failOnError: true)

		expect:
		AllowRole.DEVOPS.hasUserRole(user)
	}

	void "hasUserRole(null) is false when this == ADMIN"() {
		expect:
		!AllowRole.ADMIN.hasUserRole(null)
	}

	void "hasUserRole(user w/o roles) is false when this == ADMIN"() {
		expect:
		!AllowRole.ADMIN.hasUserRole(null)
	}

	void "hasUserRole(user with ADMIN role) is true when this == ADMIN"() {
		User user = new User()
		user.save(failOnError: true, validate: false)

        Role secRole = new Role(authority: "ROLE_ADMIN")
		secRole.save(failOnError: true)

		new UserRole(user: user, role: secRole).save(failOnError: true)

		expect:
		AllowRole.ADMIN.hasUserRole(user)
	}
}
