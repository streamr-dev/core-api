package com.unifina.domain

import grails.persistence.Entity

@Entity
class SignupInvite implements Userish {
	String code
	String email
	Boolean used
	Boolean sent
	Date dateCreated
	Date lastUpdated

    static constraints = {
		code blank: false, unique: true
		email blank: false, validator: EmailValidator.validate
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
