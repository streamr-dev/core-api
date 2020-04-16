package com.unifina.service

import com.unifina.domain.security.SignupInvite

class SignupCodeService {
	def generateSignupCode() {
		return UUID.randomUUID()
	}

	SignupInvite create(username) {
		def result = new SignupInvite(email: username,
			code: generateSignupCode(),
			sent: false,
			used: false
		)
		result.save()
		return result
	}
}
