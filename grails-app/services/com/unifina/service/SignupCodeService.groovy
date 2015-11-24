package com.unifina.service

import com.unifina.domain.security.SignupInvite

import java.util.UUID

class SignupCodeService {
	def generateSignupCode() {
		return UUID.randomUUID()
	}

	SignupInvite create(username) {
		def result = new SignupInvite(username: username,
			code: generateSignupCode(),
			sent: false,
			used: false
		)
		result.save()
		return result
	}
}
