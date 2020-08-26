package com.unifina.service

import com.unifina.domain.SignupInvite
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class SignupCodeService {
	private String generateSignupCode() {
		UUID uuid = UUID.randomUUID()
		String s = uuid.toString()
		return s
	}

	SignupInvite create(String email) {
		SignupInvite result = new SignupInvite(
			email: email,
			code: generateSignupCode(),
			sent: false,
			used: false,
		)
		result.save(failOnError: true, validate: true)
		return result
	}
}
