package com.unifina.domain.security

import com.unifina.utils.IdGenerator;

class BillingAccount {

	static hasMany = [users: SecUser]
	String chargifyCustomerId
	String chargifySubscriptionId
	String apiKey = generateApiKey()

    static constraints = {

    }

	public static String generateApiKey() {
		return IdGenerator.get() + IdGenerator.get()
	}
}
