package com.unifina.controller.security

import com.unifina.BeanMockingSpecification
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.security.TokenAuthenticator
import com.unifina.service.SessionService
import grails.test.mixin.Mock

import javax.servlet.http.HttpServletRequest
@Mock([Key, SessionService])
class TokenAuthenticatorSpec extends BeanMockingSpecification {
	TokenAuthenticator authenticator
	SessionService sessionService

	def setup() {
		authenticator = new TokenAuthenticator()
		sessionService = Mock(SessionService)
		mockBean(SessionService, sessionService)
	}

	def "no authorization string"() {
		when:
		def result = authenticator.authenticate(Stub(HttpServletRequest))

		then:
		result != null
		result.key == null
		result.secUser == null
		!result.lastAuthenticationMalformed
		result.keyMissing
	}

	def "malformed authorization string"() {
		when:
		def result = authenticator.authenticate(Stub(HttpServletRequest) {
			getHeader(_) >> "m4lf0rm3d"
		})

		then:
		result != null
		result.key == null
		result.secUser == null
		result.lastAuthenticationMalformed
		!result.keyMissing
	}

	def "valid authorization string with non-existent apiKey"() {
		when:
		def result = authenticator.authenticate(Stub(HttpServletRequest) {
			getHeader(_) >> "token apiKey"
		})

		then:
		result != null
		result.getKey() == null
		result.getSecUser() == null
		!result.lastAuthenticationMalformed
		!result.keyMissing
	}

	def "valid authorization with existent user-linked Key"() {
		setup:
		SecUser user = new SecUser()
		Key key = new Key(name: 'me-linked key', user: user).save(validate: false)

		when:
		def result = authenticator.authenticate(Stub(HttpServletRequest) {
			getHeader(_) >> "token ${key.id}"
		})

		then:
		result != null
		result.getKey() == null
		result.getSecUser().is(user)
		!result.lastAuthenticationMalformed
		!result.keyMissing
	}

	def "valid authorization with existent anonymous Key"() {
		setup:
		Key key = new Key(name: 'me-linked key').save(validate: false)

		when:
		def result = authenticator.authenticate(Stub(HttpServletRequest) {
			getHeader(_) >> "token ${key.id}"
		})

		then:
		result != null
		result.getKey().is(key)
		result.getSecUser() == null
		!result.lastAuthenticationMalformed
		!result.keyMissing
	}

	def "valid authorization string with non-existent session token"() {
		when:
		def result = authenticator.authenticate(Stub(HttpServletRequest) {
			getHeader(_) >> "Bearer session-token"
		})

		then:
		result != null
		result.getKey() == null
		result.getSecUser() == null
		!result.lastAuthenticationMalformed
		!result.keyMissing
	}
}
