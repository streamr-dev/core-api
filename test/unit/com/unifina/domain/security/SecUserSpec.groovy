package com.unifina.domain.security

import spock.lang.Specification

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
}
