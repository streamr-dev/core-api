package com.unifina.domain.security

class SignupInvite {
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
}
