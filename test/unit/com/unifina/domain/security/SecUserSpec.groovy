package com.unifina.domain.security

import grails.test.mixin.Mock
import spock.lang.Specification

@Mock(SecUserSecRole)
class SecUserSpec extends Specification {

	def "SecUsers with same id must be equal and have same hashcode"() {
		SecUser u1 = new SecUser()
		u1.id = 1234
		SecUser u2 = new SecUser()
		u2.id = 1234

		expect:
		u1.id != null
		u1.id == u2.id
		u1.equals(u2)
		u1.hashCode() == u2.hashCode()
	}

	def "SecUsers with different ids must not be equal"() {
		SecUser u1 = new SecUser()
		u1.id = 1234
		SecUser u2 = new SecUser()
		u2.id = 4321

		expect:
		u1.id != null && u2.id != null
		u1.id != u2.id
		!u1.equals(u2)
		u1.hashCode() != u2.hashCode()
	}

	def "isDevOps() == false if user does not have ROLE_DEV_OPS SecRole"() {
		expect:
		!new SecUser().devOps
	}

	def "isDevOps() == true if user has ROLE_DEV_OPS SecRole"() {
		def user = new SecUser().save(failOnError: true, validate: false)
		def role = new SecRole(authority: "ROLE_DEV_OPS").save(failOnError: true)
		new SecUserSecRole(secUser: user, secRole: role).save(failOnError: true)

		expect:
		user.devOps
	}

	def "isAdmin() == false if user does not have ROLE_ADMIN SecRole"() {
		expect:
		!new SecUser().isAdmin()
	}

	def "isAdmin() == true if user has ROLE_ADMIN SecRole"() {
		def user = new SecUser().save(failOnError: true, validate: false)
		def role = new SecRole(authority: "ROLE_ADMIN").save(failOnError: true)
		new SecUserSecRole(secUser: user, secRole: role).save(failOnError: true)

		expect:
		user.isAdmin()
	}
}
