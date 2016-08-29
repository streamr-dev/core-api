package com.unifina.domain.security

import com.unifina.utils.IdGenerator;

class BillingAccountInvite {

	BillingAccount billingAccount
	SecUser user
	String email
	Boolean used = Boolean.FALSE
	String token = generateToken()

    static constraints = {
		user(nullable: true)
		email(nullable: true)
	}

	public static String generateToken() {
		return IdGenerator.get() + IdGenerator.get()
	}
}
