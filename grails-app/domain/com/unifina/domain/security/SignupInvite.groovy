package com.unifina.domain.security

import com.unifina.security.Userish
import com.unifina.utils.UsernameValidator
import grails.persistence.Entity

@Entity
class SignupInvite implements Userish {
	String code
	String username
	Boolean used
	Boolean sent
	Date dateCreated
	Date lastUpdated

    static constraints = {
		code blank: false, unique: true
		username blank: false, validator: UsernameValidator.validate
    }

	@Override
	Userish resolveToUserish() {
		return this
	}

	// Cannot use @Override because of bug in Grails 2.3 and later
	String getClassAndId() {
		return "SignupInvite"+id.toString()
	}
}
