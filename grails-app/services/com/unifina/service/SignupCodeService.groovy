package com.unifina.service

import com.unifina.domain.security.SignupInvite
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class SignupCodeService {
	private String generateSignupCode() {
		UUID uuid = UUID.randomUUID()
		String s = uuid.toString()
		return s
	}

	SignupInvite create(String username) {
		SignupInvite result = new SignupInvite(
			username: username,
			code: generateSignupCode(),
			sent: false,
			used: false,
		)
		result.save(failOnError: true, validate: true)
		return result
	}
}
