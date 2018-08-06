package com.unifina.domain.security

import com.unifina.utils.EmailValidator
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
		username blank: false, validator: EmailValidator.validate
    }

	@Override
	Userish resolveToUserish() {
		return this
	}
}
