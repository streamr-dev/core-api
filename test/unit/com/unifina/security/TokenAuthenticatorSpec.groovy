package com.unifina.security

import com.unifina.domain.security.SecUser
import com.unifina.service.UnifinaSecurityService
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

class TokenAuthenticatorSpec extends Specification {

	UnifinaSecurityService unifinaSecurityService = Mock(UnifinaSecurityService)
	TokenAuthenticator authenticator = new TokenAuthenticator(unifinaSecurityService)

	def "no authorization string"() {
		when:
		def user = authenticator.authenticate(Stub(HttpServletRequest))

		then:
		user == null
		!authenticator.lastAuthenticationMalformed()
		!authenticator.apiKeyPresent
	}

	def "malformed authorization string"() {
		when:
		def user = authenticator.authenticate(Stub(HttpServletRequest) {
			getHeader(_) >> "m4lf0rm3d"
		})

		then:
		user == null
		authenticator.lastAuthenticationMalformed()
		!authenticator.apiKeyPresent
	}

	def "valid authorization string with non-existent apiKey"() {
		when:
		def user = authenticator.authenticate(Stub(HttpServletRequest) {
			getHeader(_) >> "token apiKey"
		})

		then:
		user == null
		!authenticator.lastAuthenticationMalformed()
		authenticator.apiKeyPresent
		1 * unifinaSecurityService.getUserByApiKey("apiKey")
	}

	def "valid authorization with existent apiKey"() {
		when:
		def user = authenticator.authenticate(Stub(HttpServletRequest) {
			getHeader(_) >> "token apiKey"
		})

		then:
		user != null
		!authenticator.lastAuthenticationMalformed()
		authenticator.apiKeyPresent
		1 * unifinaSecurityService.getUserByApiKey("apiKey") >> new SecUser()
	}
}
