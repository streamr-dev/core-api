package com.unifina.domain.security

import com.unifina.security.Userish

class SignupInvite implements Userish {
	String code
	String username
	Boolean used
	Boolean sent
	Date dateCreated
	Date lastUpdated

    static constraints = {
		code blank: false, unique: true
		username blank: false, email: true
    }

	@Override
	Userish resolveToUserish() {
		return this
	}
}
