package com.unifina.security

import com.unifina.domain.security.SecRole
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SecUserSecRole
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock(SecUserSecRole)
class AllowRoleSpec extends Specification {

	void "hasUserRole(null) is true when this == NO_ROLE_REQUIRED"() {
		expect:
		AllowRole.NO_ROLE_REQUIRED.hasUserRole(null)
	}

	void "hasUserRole(user w/o roles) is true when this == NO_ROLE_REQUIRED"() {
		expect:
		AllowRole.NO_ROLE_REQUIRED.hasUserRole(new SecUser())
	}

	void "hasUserRole(null) is false when this == DEVOPS"() {
		expect:
		!AllowRole.DEVOPS.hasUserRole(null)
	}


	void "hasUserRole(user w/o roles) is false when this == DEVOPS"() {
		expect:
		!AllowRole.DEVOPS.hasUserRole(new SecUser())
	}

	void "hasUserRole(user with DEVOPS role) is true when this == DEVOPS"() {
		SecUser user = new SecUser()
		user.save(failOnError: true, validate: false)

		SecRole secRole = new SecRole(authority: "ROLE_DEV_OPS")
		secRole.save(failOnError: true)

		new SecUserSecRole(secUser: user, secRole: secRole).save(failOnError: true)

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
		SecUser user = new SecUser()
		user.save(failOnError: true, validate: false)

		SecRole secRole = new SecRole(authority: "ROLE_ADMIN")
		secRole.save(failOnError: true)

		new SecUserSecRole(secUser: user, secRole: secRole).save(failOnError: true)

		expect:
		AllowRole.ADMIN.hasUserRole(user)
	}
}
