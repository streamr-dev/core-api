package com.unifina.domain.security

import com.unifina.utils.IdGenerator;

class BillingAccountInvite {

	static hasMany = [billingAccount: BillingAccount, users: SecUser]
	Boolean used = Boolean.FALSE
	String token = generateToken()

    static constraints = {

	}

	public static String generateToken() {
		return IdGenerator.get() + IdGenerator.get()
	}
}
